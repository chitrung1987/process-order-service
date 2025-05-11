package com.example.order.controller;

import com.example.order.dto.OrderStatusResponse;
import com.example.order.enums.OrderStatus;
import com.example.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Place /orders → 201 with created order")
    void placeOrder() throws Exception {
        when(orderService.placeOrder(5L, 7L, 3L))
                .thenAnswer(inv -> {
                    var order = new com.example.order.dao.entity.Order();
                    order.setId(99L);
                    order.setShopId(5L);
                    order.setQueueNumber(1);
                    return order;
                });

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"shopId":5,"menuItemId":7,"customerId":3}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.shopId").value(5))
                .andExpect(jsonPath("$.queueNumber").value(1));
    }

    @Test
    @DisplayName("GET status /orders/{id}/status → 200 with status")
    void getStatus() throws Exception {
        var resp = new OrderStatusResponse(OrderStatus.PLACED, 2, 1, 120);
        when(orderService.getStatus(123L)).thenReturn(resp);

        mockMvc.perform(get("/api/orders/123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.queueNumber").value(2))
                .andExpect(jsonPath("$.queuePosition").value(1))
                .andExpect(jsonPath("$.estimatedWaitSeconds").value(120));
    }

    @Test
    @DisplayName("Cancel /orders/{id} → 204 no content")
    void cancelOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/55"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Cancel /orders/{id} → 404 if not found")
    void cancelOrderNotFound() throws Exception {
        doThrow(new jakarta.persistence.EntityNotFoundException("not found"))
                .when(orderService).cancelOrder(77L);

        mockMvc.perform(delete("/api/orders/77"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
