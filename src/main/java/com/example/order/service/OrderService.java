package com.example.order.service;

import com.example.order.dao.entity.Customer;
import com.example.order.dao.entity.Order;
import com.example.order.dao.repository.CustomerRepository;
import com.example.order.dao.repository.OrderRepository;
import com.example.order.dto.OrderStatusResponse;
import com.example.order.enums.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ShopService shopService;

    public Order placeOrder(Long shopId, Long menuItemId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        int maxQ = shopService.getMaxQueues(shopId);
        Map<Integer,Long> counts = orderRepository.findByShopIdAndStatus(shopId, OrderStatus.PLACED)
                .stream()
                .collect(Collectors.groupingBy(Order::getQueueNumber, Collectors.counting()));

        for(int q=1; q<=maxQ; q++) counts.putIfAbsent(q, 0L);

        int chosenQueue = counts.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .get().getKey();

        Order order = new Order();
        order.setShopId(shopId);
        order.setMenuItemId(menuItemId);
        order.setCustomer(customer);
        order.setQueueNumber(chosenQueue);

        order = orderRepository.save(order);
        updateQueueMetrics(shopId);
        return order;
    }

    public OrderStatusResponse getStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return new OrderStatusResponse(
                order.getStatus(), order.getQueueNumber(), order.getQueuePosition(), order.getEstimatedWaitSeconds());
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        updateQueueMetrics(order.getShopId());
        // TODO: notify shop cancel the order if using messaging
    }

    private void updateQueueMetrics(Long shopId) {
        int maxQ = shopService.getMaxQueues(shopId);
        List<Order> all = orderRepository.findByShopIdAndStatus(shopId, OrderStatus.PLACED);
        // group by queueNumber, then sort & assign
        for(int q=1; q<=maxQ; q++) {
            int finalQ = q;
            List<Order> queueOrders = all.stream()
                    .filter(o -> o.getQueueNumber() == finalQ)
                    .sorted(Comparator.comparing(Order::getCreatedAt))
                    .toList();

            for(int i=0; i<queueOrders.size(); i++) {
                Order order = queueOrders.get(i);
                order.setQueuePosition(i + 1);
                order.setEstimatedWaitSeconds((i + 1) * 120);
            }
            orderRepository.saveAll(queueOrders);
        }
    }
}
