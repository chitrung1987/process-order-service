package com.example.order.dto;

import com.example.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusResponse {
    private OrderStatus status;
    private Integer queueNumber;
    private Integer queuePosition;
    private Integer estimatedWaitSeconds;
}
