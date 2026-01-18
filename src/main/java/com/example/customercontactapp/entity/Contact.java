package com.example.customercontactapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType contactType;
    
    @Column(name = "customer_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Long customerId;
    
    @Column(nullable = false)
    private String contactInfo;
    
    public Contact() {}
    
    public Contact(Long customerId, ContactType contactType, String contactInfo) {
        this.contactType = contactType;
        this.contactInfo = contactInfo;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ContactType getContactType() {
        return contactType;
    }
    
    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}