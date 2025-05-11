package com.example.order.controller;

import com.example.order.dao.entity.Order;
import com.example.order.dto.OrderStatusResponse;
import com.example.order.dto.PlaceOrderRequest;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<Order> place(@RequestBody @Valid PlaceOrderRequest req) {
        Order o = orderService.placeOrder(req.getShopId(), req.getMenuItemId(), req.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(o);
    }

    @GetMapping("/{id}/status")
    public OrderStatusResponse status(@PathVariable Long id) {
        return orderService.getStatus(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
