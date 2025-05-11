package com.example.order.dao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "shop")
@Data
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    @Column(name = "max_queues", nullable = false)
    private Integer maxQueues = 1;
    private Double latitude;
    private Double longitude;
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt;


}
