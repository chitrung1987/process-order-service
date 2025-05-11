package com.example.order.dao.repository;


import com.example.order.dao.entity.Customer;
import com.example.order.dao.entity.Order;
import com.example.order.enums.OrderStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("findByShopIdAndStatus() should return matching orders")
    void whenFindByShopIdAndStatus_thenReturnList() {
        Customer customer = new Customer();
        customer.setMobile("0900000000");
        customer.setName("Test User");
        customer.setAddress("123 Test St");
        customer = customerRepository.save(customer);

        Order order1 = new Order();
        order1.setShopId(10L);
        order1.setStatus(OrderStatus.PLACED);
        order1.setCreatedAt(Instant.now().minusSeconds(60));
        order1.setCustomer(customer);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setShopId(10L);
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setCreatedAt(Instant.now());
        order2.setCustomer(customer);
        orderRepository.save(order2);

        List<Order> placed = orderRepository.findByShopIdAndStatus(10L, OrderStatus.PLACED);
        Assertions.assertThat(placed).hasSize(1)
                .first()
                .extracting(Order::getStatus)
                .isEqualTo(OrderStatus.PLACED);
    }
}
