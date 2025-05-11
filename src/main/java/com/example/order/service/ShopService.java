package com.example.order.service;

import com.example.order.dao.entity.Shop;
import com.example.order.dao.repository.ShopRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;

    public int getMaxQueues(Long shopId) {
        return shopRepository.findById(shopId)
                .map(Shop::getMaxQueues)
                .filter(q -> q >= 1 && q <= 3)
                .orElse(1);
    }
}

