package com.example.order.dao.repository;

import com.example.order.dao.entity.Order;
import com.example.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByShopIdAndStatusOrderByCreatedAt(Long shopId, OrderStatus status);
    List<Order> findByShopIdAndStatus(Long shopId, OrderStatus status);

}

