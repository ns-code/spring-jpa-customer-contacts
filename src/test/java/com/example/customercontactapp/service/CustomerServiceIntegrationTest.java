package com.example.customercontactapp.service;

import com.example.customercontactapp.entity.Contact;
import com.example.customercontactapp.entity.ContactType;
import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void testCreateCustomer() {
        Customer customer = new Customer("John Doe");

        Customer savedCustomer = customerService.createCustomer(customer);

        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getName()).isEqualTo("John Doe");
    }

    @Test
    void testCreateCustomerWithContacts() {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));

        Customer savedCustomer = customerService.createCustomer(customer);

        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getContacts()).hasSize(2);
        assertThat(savedCustomer.getContacts().get(0).getContactType()).isEqualTo(ContactType.PHONE);
        assertThat(savedCustomer.getContacts().get(1).getContactType()).isEqualTo(ContactType.EMAIL);
    }

    @Test
    void testGetAllCustomers() {
        customerService.createCustomer(new Customer("John Doe"));
        customerService.createCustomer(new Customer("Jane Smith"));
        customerService.createCustomer(new Customer("Bob Johnson"));

        List<Customer> customers = customerService.getAllCustomers();

        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Johnson");
    }

    @Test
    void testGetAllCustomers_EmptyList() {
        List<Customer> customers = customerService.getAllCustomers();

        assertThat(customers).isEmpty();
    }

    @Test
    void testGetCustomerById() {
        Customer customer = customerService.createCustomer(new Customer("John Doe"));

        Optional<Customer> foundCustomer = customerService.getCustomerById(customer.getId());

        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getId()).isEqualTo(customer.getId());
        assertThat(foundCustomer.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testGetCustomerById_NotFound() {
        Optional<Customer> foundCustomer = customerService.getCustomerById(999L);

        assertThat(foundCustomer).isEmpty();
    }

    @Test
    void testUpdateCustomer() {
        Customer customer = customerService.createCustomer(new Customer("John Doe"));

        // update other fields as needed
        customer.setName("John Smith");
        Customer updatedCustomer = customerService.updateCustomer(customer.getId(), customer);

        assertThat(updatedCustomer.getId()).isEqualTo(customer.getId());
        assertThat(updatedCustomer.getName()).isEqualTo("John Smith");
    }

    @Test
    void testUpdateCustomer_NotFound() {
        Customer customer = new Customer("John Doe");

        assertThatThrownBy(() -> customerService.updateCustomer(999L, customer))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void testUpdateCustomerWithContacts() {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "old@example.com"));
        Customer savedCustomer = customerService.createCustomer(customer);

        Customer updateRequest = new Customer("John Smith");
        updateRequest.addContact(new Contact(customer.getId(),ContactType.PHONE, "222-222-2222"));
        updateRequest.addContact(new Contact(customer.getId(),ContactType.HOME, "123 Main St"));
        updateRequest.addContact(new Contact(customer.getId(),ContactType.EMAIL, "new@example.com"));

        Customer updatedCustomer = customerService.updateCustomerWithContacts(savedCustomer.getId(), updateRequest);

        assertThat(updatedCustomer.getName()).isEqualTo("John Smith");
        assertThat(updatedCustomer.getContacts()).hasSize(3);
        assertThat(updatedCustomer.getContacts()).extracting(Contact::getContactType)
                .containsExactlyInAnyOrder(ContactType.PHONE, ContactType.HOME, ContactType.EMAIL);
        assertThat(updatedCustomer.getContacts()).extracting(Contact::getContactInfo)
                .containsExactlyInAnyOrder("222-222-2222", "123 Main St", "new@example.com");
    }

    @Test
    void testUpdateCustomerWithContacts_ClearsOldContacts() {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "old@example.com"));
        Customer savedCustomer = customerService.createCustomer(customer);

        Customer updateRequest = new Customer("John Smith");
        updateRequest.addContact(new Contact(customer.getId(),ContactType.HOME, "123 Main St"));

        Customer updatedCustomer = customerService.updateCustomerWithContacts(savedCustomer.getId(), updateRequest);

        assertThat(updatedCustomer.getContacts()).hasSize(1);
        assertThat(updatedCustomer.getContacts().get(0).getContactType()).isEqualTo(ContactType.HOME);
    }

    @Test
    void testUpdateCustomerWithContacts_EmptyContacts() {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        Customer savedCustomer = customerService.createCustomer(customer);

        Customer updateRequest = new Customer("John Smith");

        Customer updatedCustomer = customerService.updateCustomerWithContacts(savedCustomer.getId(), updateRequest);

        assertThat(updatedCustomer.getName()).isEqualTo("John Smith");
        assertThat(updatedCustomer.getContacts()).isEmpty();
    }

    @Test
    void testDeleteCustomer() {
        Customer customer = customerService.createCustomer(new Customer("John Doe"));
        Long customerId = customer.getId();

        customerService.deleteCustomer(customerId);

        Optional<Customer> deletedCustomer = customerService.getCustomerById(customerId);
        assertThat(deletedCustomer).isEmpty();
    }

    @Test
    void testDeleteCustomer_CascadesToContacts() {
        Customer customer = new Customer("John Doe");
        customer.addContact(new Contact(customer.getId(),ContactType.PHONE, "111-111-1111"));
        customer.addContact(new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));
        Customer savedCustomer = customerService.createCustomer(customer);

        customerService.deleteCustomer(savedCustomer.getId());

        Optional<Customer> deletedCustomer = customerService.getCustomerById(savedCustomer.getId());
        assertThat(deletedCustomer).isEmpty();
    }

    @Test
    void testCustomerContactsRelationship() {
        Customer customer = new Customer("John Doe");
        Contact contact = new Contact(null, ContactType.PHONE, "123-456-7890");
        customer.addContact(contact);
        Customer savedCustomer = customerService.createCustomer(customer);

        Contact savedContact = savedCustomer.getContacts().stream().filter(c -> c.getContactType() == contact.getContactType() && c.getContactInfo().equals(contact.getContactInfo())).findFirst().orElse(null);

        assertThat(savedCustomer.getContacts()).hasSize(1);
        assertThat(savedContact.getContactType()).isEqualTo(ContactType.PHONE);
    }
}