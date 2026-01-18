package com.example.customercontactapp.service;

import com.example.customercontactapp.entity.Contact;
import com.example.customercontactapp.entity.Customer;
import com.example.customercontactapp.repository.ContactRepository;
import com.example.customercontactapp.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactService {
    
    private final ContactRepository contactRepository;
    private final CustomerRepository customerRepository;
    
    public ContactService(ContactRepository contactRepository, CustomerRepository customerRepository) {
        this.contactRepository = contactRepository;
        this.customerRepository = customerRepository;
    }
    
    public Contact createContact(Long customerId, Contact contact) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        contact.setCustomerId(customerId);
        customer.addContact(contact);
        Customer savedCustomer = customerRepository.save(customer);
        return savedCustomer.getContacts().stream().filter(c -> c.getContactType() == contact.getContactType() && c.getContactInfo().equals(contact.getContactInfo())).findFirst().orElse(null);
    }
    
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }
    
    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }
    
    public List<Contact> getContactsByCustomerId(Long customerId) {
        return contactRepository.findByCustomerId(customerId);
    }
    
    public Contact updateContact(Long id, Contact contactDetails) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
        
        contact.setContactType(contactDetails.getContactType());
        contact.setContactInfo(contactDetails.getContactInfo());
        return contactRepository.save(contact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}