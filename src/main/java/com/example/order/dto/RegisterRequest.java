package com.example.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String mobile;
    @NotBlank private String name;
    private String address;
}