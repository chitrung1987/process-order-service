# process-order-service
## 1. Project Structure

> **Note:** We don’t yet have separate customer-service or shop-service microservices.  
> For now, customer and shop tables exist only to provide dump data.

```
src/
├── controller/        # REST controllers
├── service/           # Business logic
├── dao/               # Data-access layer
├── exception/         # Global exceptions & error responses
```

- **controller layer**  
  Contains all Spring MVC controllers (entry points for HTTP requests).

- **service layer**  
  Implements core business logic.

- **dao layer**  
  Interacts with PostgreSQL via Spring Data / JDBC templates.

- **exception package**  
  Catches & handles exceptions globally, returning standardized error payloads.

- **diagram folder**  
  Contains `*.png` files for your ERD, sequence and use-case diagrams.

- **deployment folder**  
  Contains script to deploy to aws ecs fargate.

---

## 2. Testing

1. Clone the repo and run:

   ```bash
    docker compose down
    docker compose up --build -d
   ```
      This starts the application and provisions the database.
Endpoints:
  - POST http://localhost:8080/api/orders
  - GET http://localhost:8080/api/orders/{id}/status
  - DELETE http://localhost:8080/api/orders/{id}

2. For local testing, make the smoke-test script executable and run it:

   ```bash
   chmod +x test-api.sh
   ./test-api.sh
   ```

3. Execute unit tests:

   ```bash
   mvn test
   ```

4. (AWS) testing, Import `coffee-shop.postman_collection.json` into Postman for API testing.

5. For load/stress testing, use JMeter

---

## 3. For deployment to ecs fargate

- Run push-to-ecr.sh file to push image to ECR
- Run setup-ecs-cluster.sh to setup ecs cluster and create vpc, subnet
- Run deploy-fargate.sh deploy task into cluster
- Maybe after deployment the task to ecs, you will get error related to service permission. In this scenario, you can use ecs-trust.json and run 2 commands below to assign permission. Otherwise, ignore these commands
```bash
   aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document file://ecs-trust.json
   ```
```bash
   aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
   ```

## NOTE: You need to change AWS_ACCOUNT_ID to your account


---

## 4. Coding, Naming & Technology Standards

- **Language / Framework**  
  Java 17 + Spring Boot 3.4

- **Persistence**  
  PostgreSQL

- **Migrations**  
  Liquibase changelogs  
  - One “master” changelog  
  - Contains `createTable`, `insert`, and `resync sequence` changesets

- **Containerization**  
  - Multi-stage `Dockerfile`  
  - Configuration via environment variables

- **AWS**  
  - Container images in ECR  
  - ECS / Fargate tasks

- **REST Naming**  
  - **Resources are plural:** `/customers`, `/shops/nearby`, `/orders`  
  - **Use HTTP verbs**: `POST`, `GET`, `DELETE`

- **Logging**  
  SLF4J + Logback with sensible levels:  
  - `INFO` for normal operations  
  - `WARN` for recoverable issues  
  - `ERROR` for unexpected failures

---

## 4. Future Security Solution

We’ll integrate Spring Security and an IDP (Keycloak/Okta) to manage keys, authentication & authorization:

### Authentication

1. **Register**  
   - Customer submits mobile → we send OTP via SMS.  
2. **Verify**  
   - Customer submits OTP → we issue a JWT (expires in 1 hour).

### Authorization

- **Bearer JWT** in `Authorization` header on all APIs  
- Spring Security filter validates token & populates `SecurityContext`

### Other Concerns

- **Transport:** TLS everywhere (HTTPS)  
- **Secret Management:** AWS Secrets Manager for DB creds & JWT signing keys  
- **CORS:** Only allow your mobile app’s origin

---

## 5. Enhance performance
- **Caching:** : use Redis
- Seperate write/read database to avoid overload
- Use CloudFront to do CDN.
- Setup Route53 follow geolocation policy
- Can deploy backend on multi-region to ensure HA


## 6. Full Services (Future)

We plan three microservices:

1. **Customer Service**  
2. **Shop Service**  
3. **Process-Order Service** (current focus)

Once all three exist, the complete API surface will include:

| Method | Path                                     | Description                                | Auth   | Request DTO                          | Response DTO       |
|--------|------------------------------------------|--------------------------------------------|--------|--------------------------------------|--------------------|
| POST   | `/api/customers`                         | Register a new customer                    | Public | `{ mobile, name, address }`          | `Customer`         |
| POST   | `/api/auth/otp/send`                     | Send OTP to mobile                         | Public | `{ mobile }`                         | `202 Accepted`     |
| POST   | `/api/auth/otp/verify`                   | Verify OTP & issue JWT                     | Public | `{ mobile, otp }`                    | `{ token, expires }` |
| GET    | `/api/shops/nearby?lat={lat}&lon={lon}&limit={n}` | Find N nearest shops                       | Bearer | —                                    | `List<ShopGeoDTO>` |
| POST   | `/api/orders`                            | Place a new order                          | Bearer | `{ shopId, menuItemId }`             | `Order`            |
| GET    | `/api/orders/{id}/status`                | Get status, queue position & ETA           | Bearer | —                                    | `OrderStatusDTO`   |
| DELETE | `/api/orders/{id}`                       | Cancel order (exit queue)                  | Bearer | —                                    | `204 No Content`   |
