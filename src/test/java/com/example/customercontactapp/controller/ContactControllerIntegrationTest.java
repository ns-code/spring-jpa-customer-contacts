package com.example.customercontactapp.controller;

import com.example.customercontactapp.entity.Contact;
import com.example.customercontactapp.entity.ContactType;
import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.ContactRepository;
import com.example.customercontactapp.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContactControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        customerRepository.deleteAll();
    }    

    @Test
    void testCreateContact() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = new Contact(customer.getId(),ContactType.PHONE, "123-456-7890");

        mockMvc.perform(post("/api/contacts/customer/{customerId}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isCreated())
                // .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.contactType").value("PHONE"))
                .andExpect(jsonPath("$.contactInfo").value("123-456-7890"));
    }

    @Test
    void testCreateContact_CustomerNotFound() throws Exception {
        Contact contact = new Contact(null, ContactType.PHONE, "123-456-7890");

        mockMvc.perform(post("/api/contacts/customer/{customerId}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllContacts() throws Exception {
        Customer customer1 = customerRepository.save(new Customer("John Doe"));
        Customer customer2 = customerRepository.save(new Customer("Jane Smith"));
        
        customer1.addContact(new Contact(customer1.getId(),ContactType.PHONE, "111-111-1111"));
        customer2.addContact(new Contact(customer2.getId(),ContactType.EMAIL, "jane@example.com"));
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetContactById() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));
        Customer savedCustomer = customerRepository.save(customer);
        Contact savedContact = savedCustomer.getContacts().get(0);

        mockMvc.perform(get("/api/contacts/{id}", savedContact.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedContact.getId()))
                .andExpect(jsonPath("$.contactType").value("PHONE"))
                .andExpect(jsonPath("$.contactInfo").value("123-456-7890"));
    }

    @Test
    void testGetContactById_NotFound() throws Exception {
        mockMvc.perform(get("/api/contacts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetContactsByCustomerId() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));
        customer.addContact(new Contact(customer.getId(),ContactType.HOME, "123 Main St"));
        Customer savedCustomer = customerRepository.save(customer);

        mockMvc.perform(get("/api/contacts/customer/{customerId}", savedCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].contactType").value("PHONE"))
                .andExpect(jsonPath("$[1].contactType").value("EMAIL"))
                .andExpect(jsonPath("$[2].contactType").value("HOME"));
    }

    @Test
    void testGetContactsByCustomerId_EmptyList() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        mockMvc.perform(get("/api/contacts/customer/{customerId}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testUpdateContact() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        Customer savedCustomer = customerRepository.save(customer);
        Contact savedContact = savedCustomer.getContacts().get(0);

        Contact updatedContact = new Contact(customer.getId(),ContactType.EMAIL, "updated@example.com");

        mockMvc.perform(put("/api/contacts/{id}", savedContact.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedContact.getId()))
                .andExpect(jsonPath("$.contactType").value("EMAIL"))
                .andExpect(jsonPath("$.contactInfo").value("updated@example.com"));
    }

    @Test
    void testUpdateContact_NotFound() throws Exception {
        Contact contact = new Contact(null,ContactType.PHONE, "123-456-7890");

        mockMvc.perform(put("/api/contacts/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteContact() throws Exception {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        Customer savedCustomer = customerRepository.save(customer);
        Contact savedContact = savedCustomer.getContacts().get(0);

        mockMvc.perform(delete("/api/contacts/{id}", savedContact.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contacts/{id}", savedContact.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleContactTypes() throws Exception {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        Contact phoneContact = new Contact(customer.getId(),ContactType.PHONE, "123-456-7890");
        Contact homeContact = new Contact(customer.getId(),ContactType.HOME, "123 Main St");
        Contact emailContact = new Contact(customer.getId(),ContactType.EMAIL, "john@example.com");

        mockMvc.perform(post("/api/contacts/customer/{customerId}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(phoneContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactType").value("PHONE"));

        mockMvc.perform(post("/api/contacts/customer/{customerId}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(homeContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactType").value("HOME"));

        mockMvc.perform(post("/api/contacts/customer/{customerId}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactType").value("EMAIL"));

        mockMvc.perform(get("/api/contacts/customer/{customerId}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}