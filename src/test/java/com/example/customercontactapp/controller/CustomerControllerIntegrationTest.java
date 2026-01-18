package com.example.customercontactapp.controller;

import com.example.customercontactapp.entity.Contact;
import com.example.customercontactapp.entity.ContactType;
import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void testCreateCustomer() throws Exception {
        Customer customer = new Customer("John Doe");

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        customerRepository.save(new Customer("John Doe"));
        customerRepository.save(new Customer("Jane Smith"));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void testGetCustomerById() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        mockMvc.perform(get("/api/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCustomer() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        
        Customer updatedCustomer = new Customer("John Smith");

        mockMvc.perform(put("/api/customers/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Smith"));
    }

    @Test
    void testUpdateCustomer_NotFound() throws Exception {
        Customer customer = new Customer("John Doe");

        mockMvc.perform(put("/api/customers/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCustomerWithContacts() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "old@example.com"));
        Customer savedCustomer = customerRepository.save(customer);

        Customer updateRequest = new Customer("John Smith");
        updateRequest.setContacts(new ArrayList<>());
        updateRequest.addContact(new Contact(customer.getId(),ContactType.PHONE, "222-222-2222"));
        updateRequest.addContact(new Contact(customer.getId(),ContactType.HOME, "123 Main St"));

        mockMvc.perform(put("/api/customers/{id}/with-contacts", savedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.contacts", hasSize(2)))
                .andExpect(jsonPath("$.contacts[0].contactType").value("PHONE"))
                .andExpect(jsonPath("$.contacts[0].contactInfo").value("222-222-2222"))
                .andExpect(jsonPath("$.contacts[1].contactType").value("HOME"))
                .andExpect(jsonPath("$.contacts[1].contactInfo").value("123 Main St"));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        mockMvc.perform(delete("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", customer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCustomer_CascadesToContacts() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));
        Customer savedCustomer = customerRepository.save(customer);

        mockMvc.perform(delete("/api/customers/{id}", savedCustomer.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", savedCustomer.getId()))
                .andExpect(status().isNotFound());
    }
}