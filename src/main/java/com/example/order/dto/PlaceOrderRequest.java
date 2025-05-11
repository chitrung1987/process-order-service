package com.example.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull
    private Long shopId;
    @NotNull private Long menuItemId;
    @NotNull private Long customerId;
}
