package com.example.order.service;

import com.example.order.dao.entity.Customer;
import com.example.order.dao.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer register(String mobile, String name, String address) {
        if (customerRepository.findByMobile(mobile).isPresent())
            throw new IllegalArgumentException("Mobile already registered");
        Customer customer = new Customer();
        customer.setMobile(mobile);
        customer.setName(name);
        customer.setAddress(address);
        return customerRepository.save(customer);
    }
}

