package com.example.order.service;

import com.example.order.dao.entity.Customer;
import com.example.order.dao.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repo;

    @InjectMocks
    private CustomerService service;

    @Test
    @DisplayName("register() saves a new customer when mobile not taken")
    void registerNewCustomer() {
        when(repo.findByMobile("0912345678")).thenReturn(Optional.empty());
        when(repo.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Customer result = service.register("0912345678", "Bob", "Addr");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Bob");
        verify(repo).findByMobile("0912345678");
        verify(repo).save(any());
    }

    @Test
    @DisplayName("register() throws when mobile already exists")
    void registerDuplicateThrows() {
        when(repo.findByMobile("0912")).thenReturn(Optional.of(new Customer()));
        assertThatThrownBy(() -> service.register("0912", "X", "Y"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mobile already registered");
    }
}
