package com.example.order.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "customer")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mobile;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt;

}
