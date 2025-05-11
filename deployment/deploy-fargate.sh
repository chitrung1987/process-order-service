#!/usr/bin/env bash
# AWS ECS Deployment Script with Auto VPC/Subnet Creation

# Fail fast on errors, undefined vars, and failed pipes
set -euo pipefail

# Disable AWS CLI pager (avoids "pager error" in scripts)
export AWS_PAGER=""

# Configuration
REGION="us-east-1"
ECS_CLUSTER_NAME="process-order-cluster"
TASK_DEFINITION_NAME="order-task"
SERVICE_NAME="order-service"
APP_IMAGE="AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/process-order-service:latest"
DB_IMAGE="postgres:15"
CONTAINER_NAME_APP="process-order-service"
CONTAINER_NAME_DB="order-db"

# Step 1: Login to ECR
aws ecr get-login-password \
    --region "$REGION" | \
  docker login --username AWS --password-stdin 122610499913.dkr.ecr.us-east-1.amazonaws.com

# Step 2: Create VPC Infrastructure
echo "Creating VPC resources..."
VPC_ID=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 --query 'Vpc.VpcId' --output text --region "$REGION")
aws ec2 modify-vpc-attribute --vpc-id "$VPC_ID" --enable-dns-support "{\"Value\":true}" --region "$REGION"
aws ec2 modify-vpc-attribute --vpc-id "$VPC_ID" --enable-dns-hostnames "{\"Value\":true}" --region "$REGION"

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text --region "$REGION")
aws ec2 attach-internet-gateway --vpc-id "$VPC_ID" --internet-gateway-id "$IGW_ID" --region "$REGION"

# Create Public Subnet
SUBNET_ID=$(aws ec2 create-subnet \
    --vpc-id "$VPC_ID" \
    --cidr-block 10.0.0.0/24 \
    --availability-zone "${REGION}a" \
    --query 'Subnet.SubnetId' \
    --output text \
    --region "$REGION")

# Create Route Table
ROUTE_TABLE_ID=$(aws ec2 create-route-table --vpc-id "$VPC_ID" --query 'RouteTable.RouteTableId' --output text --region "$REGION")
aws ec2 create-route \
    --route-table-id "$ROUTE_TABLE_ID" \
    --destination-cidr-block 0.0.0.0/0 \
    --gateway-id "$IGW_ID" \
    --region "$REGION"
aws ec2 associate-route-table \
    --subnet-id "$SUBNET_ID" \
    --route-table-id "$ROUTE_TABLE_ID" \
    --region "$REGION"

# Create Security Group
SECURITY_GROUP_ID=$(aws ec2 create-security-group \
    --group-name "ECS-SecurityGroup" \
    --description "ECS Security Group" \
    --vpc-id "$VPC_ID" \
    --query 'GroupId' \
    --output text \
    --region "$REGION")

# Add Security Group Rules
aws ec2 authorize-security-group-ingress \
    --group-id "$SECURITY_GROUP_ID" \
    --protocol tcp \
    --port 8080 \
    --cidr 0.0.0.0/0 \
    --region "$REGION"
aws ec2 authorize-security-group-ingress \
    --group-id "$SECURITY_GROUP_ID" \
    --protocol tcp \
    --port 5432 \
    --source-group "$SECURITY_GROUP_ID" \
    --region "$REGION"


# Step 4: Register Task Definition
cat <<EOF > task-definition.json
{
    "family": "$TASK_DEFINITION_NAME",
    "networkMode": "awsvpc",
    "containerDefinitions": [
        {
            "name": "$CONTAINER_NAME_APP",
            "image": "$APP_IMAGE",
            "portMappings": [
                {"containerPort": 8080, "hostPort": 8080, "protocol": "tcp"}
            ],
            "environment": [
                {"name": "DB_HOST", "value": "localhost"},
                {"name": "DB_PORT", "value": "5432"},
                {"name": "DB_NAME", "value": "order_db"},
                {"name": "DB_USERNAME", "value": "postgres"},
                {"name": "DB_PASSWORD", "value": "password"},
                {"name": "SERVER_PORT", "value": "8080"}
            ],
            "essential": true,
            "dependsOn": [{"containerName": "$CONTAINER_NAME_DB", "condition": "HEALTHY"}]
        },
        {
            "name": "$CONTAINER_NAME_DB",
            "image": "$DB_IMAGE",
            "portMappings": [
                {"containerPort": 5432, "hostPort": 5432, "protocol": "tcp"}
            ],
            "environment": [
                {"name": "POSTGRES_USER", "value": "postgres"},
                {"name": "POSTGRES_PASSWORD", "value": "password"},
                {"name": "POSTGRES_DB", "value": "order_db"}
            ],
            "essential": true,
            "healthCheck": {"command": ["CMD-SHELL", "pg_isready -U postgres"], "interval": 30, "timeout": 5, "retries": 3}
        }
    ],
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "1024",
    "memory": "3072",
    "executionRoleArn": "ecsTaskExecutionRole"
}
EOF

aws ecs register-task-definition --cli-input-json file://task-definition.json --region "$REGION"

# Step 5: Deploy Service
SERVICE_NAME_TEXT=$(aws ecs describe-services \
  --cluster  "$ECS_CLUSTER_NAME" \
  --services "$SERVICE_NAME" \
  --region   "$REGION" \
  --query    'services[0].serviceName' \
  --output   text)

if [ "$SERVICE_NAME_TEXT" = "None" ] || [ "$SERVICE_NAME_TEXT" = "null" ]; then
  echo "Service not found—creating ECS service…"
  aws ecs create-service \
    --cluster            "$ECS_CLUSTER_NAME" \
    --service-name       "$SERVICE_NAME" \
    --task-definition    "$TASK_DEFINITION_NAME" \
    --launch-type        FARGATE \
    --desired-count      1 \
    --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_ID],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
    --region             "$REGION"
else
  echo "Service exists—updating ECS service…"
  aws ecs update-service \
    --cluster         "$ECS_CLUSTER_NAME" \
    --service         "$SERVICE_NAME" \
    --task-definition "$TASK_DEFINITION_NAME" \
    --region          "$REGION"
fi

echo "Deployment complete!"

