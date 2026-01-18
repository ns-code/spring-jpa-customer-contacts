package com.example.customercontactapp.service;

import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        
        customer.setName(customerDetails.getName());
        // customer.setContacts(customerDetails.getContacts());
        customer.getContacts().clear();
        if (customerDetails.getContacts() != null) {
            customerDetails.getContacts().forEach(customer::addContact);
        }       
        log.info(">> cust: ()", customer.getContacts());
        return customerRepository.save(customer);
    }

    public Customer updateCustomerWithContacts(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        
        customer.setName(customerDetails.getName());
        
        // Clear existing contacts and add new ones
        customer.getContacts().clear();
        if (customerDetails.getContacts() != null) {
            customerDetails.getContacts().forEach(customer::addContact);
        }
        
        return customerRepository.save(customer);
    }    
    
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}