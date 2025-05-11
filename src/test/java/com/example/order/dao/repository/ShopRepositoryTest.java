package com.example.order.dao.repository;

import com.example.order.dao.entity.Shop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ShopRepositoryTest {

    @Autowired
    private ShopRepository shopRepository;

    @Test
    @DisplayName("save() and findById() should persist and retrieve a Shop")
    void whenSave_thenFindById() {
        Shop shop = new Shop();
        shop.setName("Corner Cafe");
        shop.setLocation("1st Ave");
        shop.setMaxQueues(2);
        shop.setLatitude(10.0);
        shop.setLongitude(20.0);

        Shop saved = shopRepository.save(shop);
        assertThat(shopRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Shop::getName, Shop::getLocation, Shop::getMaxQueues)
                .containsExactly("Corner Cafe", "1st Ave", 2);
    }
}
