package com.example.order.service;

import com.example.order.dao.entity.Shop;
import com.example.order.dao.repository.ShopRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopService shopService;

    @Test
    @DisplayName("getMaxQueues() returns stored value when within 1–3")
    void returnsValidMaxQueues() {
        Shop shop = new Shop();
        shop.setMaxQueues(2);
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        int maxQueues = shopService.getMaxQueues(1L);

        assertThat(maxQueues).isEqualTo(2);
    }

    @Test
    @DisplayName("getMaxQueues() defaults to 1 when shop not found")
    void defaultsToOneWhenNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        int maxQueues = shopService.getMaxQueues(99L);

        assertThat(maxQueues).isEqualTo(1);
    }

    @Test
    @DisplayName("getMaxQueues() defaults to 1 when value out of range")
    void defaultsToOneWhenOutOfRange() {
        Shop shop = new Shop();
        shop.setMaxQueues(5);
        when(shopRepository.findById(2L)).thenReturn(Optional.of(shop));

        int maxQueues = shopService.getMaxQueues(2L);

        assertThat(maxQueues).isEqualTo(1);
    }
}
