package com.example.order.service;

import com.example.order.dao.entity.Customer;
import com.example.order.dao.entity.Order;
import com.example.order.dao.repository.CustomerRepository;
import com.example.order.dao.repository.OrderRepository;
import com.example.order.dto.OrderStatusResponse;
import com.example.order.enums.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ShopService shopService;

    @InjectMocks private OrderService service;

    @Test
    @DisplayName("placeOrder() assigns to the least-loaded queue")
    void placeOrderAssignsCorrectQueue() {
        Customer customer = new Customer(); customer.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(shopService.getMaxQueues(10L)).thenReturn(2);

        Order oder1 = new Order(); oder1.setQueueNumber(1);
        Order order2 = new Order(); order2.setQueueNumber(2);
        Order order3 = new Order(); order3.setQueueNumber(2);
        when(orderRepository.findByShopIdAndStatus(10L, OrderStatus.PLACED))
                .thenReturn(List.of(oder1, order2, order3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });
        Order placed = service.placeOrder(10L, 77L, 1L);

        // then least-loaded queue is 1
        assertThat(placed.getQueueNumber()).isEqualTo(1);
        verify(orderRepository).save(any());
        verify(orderRepository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("getStatus() returns correct response")
    void getStatusReturnsDto() {
        Order order = new Order();
        order.setId(5L);
        order.setStatus(OrderStatus.PLACED);
        order.setQueueNumber(1);
        order.setQueuePosition(3);
        order.setEstimatedWaitSeconds(240);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        OrderStatusResponse resp = service.getStatus(5L);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(resp.getQueueNumber()).isEqualTo(1);
        assertThat(resp.getQueuePosition()).isEqualTo(3);
        assertThat(resp.getEstimatedWaitSeconds()).isEqualTo(240);
    }

    @Test
    @DisplayName("getStatus() throws if order not found")
    void getStatusNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getStatus(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("cancelOrder() updates status and recalculates queue for one queue")
    void cancelOrderUpdates() {
        Order order = new Order();
        order.setId(8L);
        order.setShopId(20L);
        order.setStatus(OrderStatus.PLACED);
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        // stub 1 queue
        when(shopService.getMaxQueues(20L)).thenReturn(1);
        // no other placed orders after cancellation
        when(orderRepository.findByShopIdAndStatus(20L, OrderStatus.PLACED))
                .thenReturn(Collections.emptyList());
        service.cancelOrder(8L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
        verify(orderRepository).findByShopIdAndStatus(20L, OrderStatus.PLACED);
        verify(orderRepository).saveAll(anyList());
    }

}
