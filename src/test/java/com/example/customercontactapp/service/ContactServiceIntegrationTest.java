package com.example.customercontactapp.service;

import com.example.customercontactapp.entity.Contact;
import com.example.customercontactapp.entity.ContactType;
import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.ContactRepository;
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
class ContactServiceIntegrationTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ContactRepository contactRepository;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void testCreateContact() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = new Contact(customer.getId(),ContactType.PHONE, "123-456-7890");

        Contact savedContact = contactService.createContact(customer.getId(), contact);

        assertThat(savedContact.getId()).isNotNull();
        assertThat(savedContact.getContactType()).isEqualTo(ContactType.PHONE);
        assertThat(savedContact.getContactInfo()).isEqualTo("123-456-7890");
        assertThat(savedContact.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void testCreateContact_CustomerNotFound() {
        Contact contact = new Contact(null,ContactType.PHONE, "123-456-7890");

        assertThatThrownBy(() -> contactService.createContact(999L, contact))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void testCreateMultipleContactsForSameCustomer() {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        Contact phoneContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));
        Contact emailContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));
        Contact homeContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.HOME, "123 Main St"));

        List<Contact> contacts = contactService.getContactsByCustomerId(customer.getId());

        assertThat(contacts).hasSize(3);
        assertThat(contacts).extracting(Contact::getContactType)
                .containsExactlyInAnyOrder(ContactType.PHONE, ContactType.EMAIL, ContactType.HOME);
    }

    @Test
    void testGetAllContacts() {
        Customer customer1 = customerRepository.save(new Customer("John Doe"));
        Customer customer2 = customerRepository.save(new Customer("Jane Smith"));

        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.PHONE, "111-111-1111"));
        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.EMAIL, "john@example.com"));
        contactService.createContact(customer2.getId(), new Contact(customer2.getId(),ContactType.PHONE, "222-222-2222"));

        List<Contact> allContacts = contactService.getAllContacts();

        assertThat(allContacts).hasSize(3);
    }

    @Test
    void testGetAllContacts_EmptyList() {
        List<Contact> contacts = contactService.getAllContacts();

        assertThat(contacts).isEmpty();
    }

    @Test
    void testGetContactById() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));

        Optional<Contact> foundContact = contactService.getContactById(contact.getId());

        assertThat(foundContact).isPresent();
        assertThat(foundContact.get().getId()).isEqualTo(contact.getId());
        assertThat(foundContact.get().getContactType()).isEqualTo(ContactType.PHONE);
        assertThat(foundContact.get().getContactInfo()).isEqualTo("123-456-7890");
    }

    @Test
    void testGetContactById_NotFound() {
        Optional<Contact> foundContact = contactService.getContactById(999L);

        assertThat(foundContact).isEmpty();
    }

    @Test
    void testGetContactsByCustomerId() {
        Customer customer1 = customerRepository.save(new Customer("John Doe"));
        Customer customer2 = customerRepository.save(new Customer("Jane Smith"));

        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.PHONE, "111-111-1111"));
        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.EMAIL, "john@example.com"));
        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.HOME, "123 Main St"));
        contactService.createContact(customer2.getId(), new Contact(customer2.getId(),ContactType.PHONE, "222-222-2222"));

        List<Contact> customer1Contacts = contactService.getContactsByCustomerId(customer1.getId());

        assertThat(customer1Contacts)
            .hasSize(3)
            .allMatch(c -> c.getCustomerId().equals(customer1.getId()));
    }

    @Test
    void testGetContactsByCustomerId_EmptyList() {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        List<Contact> contacts = contactService.getContactsByCustomerId(customer.getId());

        assertThat(contacts).isEmpty();
    }

    @Test
    void testUpdateContact() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));

        Contact updateRequest = new Contact(customer.getId(),ContactType.EMAIL, "newemail@example.com");
        Contact updatedContact = contactService.updateContact(contact.getId(), updateRequest);

        assertThat(updatedContact.getId()).isEqualTo(contact.getId());
        assertThat(updatedContact.getContactType()).isEqualTo(ContactType.EMAIL);
        assertThat(updatedContact.getContactInfo()).isEqualTo("newemail@example.com");
    }

    @Test
    void testUpdateContact_NotFound() {
        Contact contact = new Contact(null,ContactType.PHONE, "123-456-7890");

        assertThatThrownBy(() -> contactService.updateContact(999L, contact))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contact not found with id: 999");
    }

    @Test
    void testUpdateContact_ChangeContactType() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));

        Contact updateRequest = new Contact(customer.getId(),ContactType.HOME, "456 Oak Avenue");
        Contact updatedContact = contactService.updateContact(contact.getId(), updateRequest);

        assertThat(updatedContact.getContactType()).isEqualTo(ContactType.HOME);
        assertThat(updatedContact.getContactInfo()).isEqualTo("456 Oak Avenue");
    }

    @Test
    void testDeleteContact() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));

        contactService.deleteContact(contact.getId());

        Optional<Contact> deletedContact = contactService.getContactById(contact.getId());
        assertThat(deletedContact).isEmpty();
    }

    @Test
    void testDeleteContact_DoesNotDeleteCustomer() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));

        contactService.deleteContact(contact.getId());

        Optional<Customer> foundCustomer = customerRepository.findById(customer.getId());
        assertThat(foundCustomer).isPresent();
    }

    @Test
    void testAllContactTypes() {
        Customer customer = customerRepository.save(new Customer("John Doe"));

        Contact phoneContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));
        Contact homeContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.HOME, "123 Main St"));
        Contact emailContact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.EMAIL, "john@example.com"));

        assertThat(phoneContact.getContactType()).isEqualTo(ContactType.PHONE);
        assertThat(homeContact.getContactType()).isEqualTo(ContactType.HOME);
        assertThat(emailContact.getContactType()).isEqualTo(ContactType.EMAIL);
    }

    @Test
    void testContactCustomerRelationship() {
        Customer customer = customerRepository.save(new Customer("John Doe"));
        Contact contact = contactService.createContact(customer.getId(), 
                new Contact(customer.getId(),ContactType.PHONE, "123-456-7890"));      
        System.out.println(">> Created contact with ID: " + contact.getId() + " for customer ID: " + customer.getId());                

        Optional<Contact> foundContact = contactService.getContactById(contact.getId());

        assertThat(foundContact).isPresent();
        assertThat(foundContact.get().getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void testMultipleCustomersWithContacts() {
        Customer customer1 = customerRepository.save(new Customer("John Doe"));
        Customer customer2 = customerRepository.save(new Customer("Jane Smith"));

        contactService.createContact(customer1.getId(), new Contact(customer1.getId(),ContactType.PHONE, "111-111-1111"));
        contactService.createContact(customer2.getId(), new Contact(customer2.getId(),ContactType.PHONE, "222-222-2222"));

        List<Contact> customer1Contacts = contactService.getContactsByCustomerId(customer1.getId());    
        List<Contact> customer2Contacts = contactService.getContactsByCustomerId(customer2.getId());

        assertThat(customer1Contacts).hasSize(1);
        assertThat(customer2Contacts).hasSize(1);
        assertThat(customer1Contacts.get(0).getContactInfo()).isEqualTo("111-111-1111");
        assertThat(customer2Contacts.get(0).getContactInfo()).isEqualTo("222-222-2222");
    }
}