package com.example.order.dao.repository;

import com.example.order.dao.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("save() and findById() should persist and retrieve a Customer")
    void whenSave_thenFindById() {
        Customer customer = new Customer();
        customer.setMobile("0912345678");
        customer.setName("John Doe");
        customer.setAddress("123 Main St");

        Customer saved = customerRepository.save(customer);
        Optional<Customer> found = customerRepository.findById(saved.getId());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Customer::getMobile, Customer::getName, Customer::getAddress)
                .containsExactly("0912345678", "John Doe", "123 Main St");
    }

    @Test
    @DisplayName("findByMobile() should return matching Customer")
    void whenFindByMobile_thenReturnCustomer() {
        Customer customer = new Customer();
        customer.setMobile("0999888777");
        customer.setName("Alice");
        customer.setAddress("456 Elm St");
        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByMobile("0999888777");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }
}
