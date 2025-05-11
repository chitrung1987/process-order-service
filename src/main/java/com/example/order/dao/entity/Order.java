package com.example.order.dao.entity;

import com.example.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shopId;
    private Long menuItemId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;
    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber = 1;
    private Integer queuePosition;
    private Integer estimatedWaitSeconds;
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt;
}

