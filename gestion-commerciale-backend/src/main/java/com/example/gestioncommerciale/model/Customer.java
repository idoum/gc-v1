/*
 * @path src/main/java/com/example/gestioncommerciale/model/Customer.java
 * @description Entité Customer pour le module CRM avec gestion complète des informations client
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "code"),
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "siret")
       },
       indexes = {
           @Index(name = "idx_customer_code", columnList = "code"),
           @Index(name = "idx_customer_email", columnList = "email"),
           @Index(name = "idx_customer_status", columnList = "status"),
           @Index(name = "idx_customer_company_name", columnList = "companyName")
       })
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"addresses", "orders"})
@ToString(exclude = {"addresses", "orders"})
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String code; // Code client unique (ex: CLI-2025-0001)
    
    @Column(nullable = false, length = 150)
    private String companyName;
    
    @Column(length = 100)
    private String contactFirstName;
    
    @Column(length = 100)
    private String contactLastName;
    
    @Column(length = 150)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 20)
    private String mobile;
    
    @Column(length = 20)
    private String siret;
    
    @Column(length = 15)
    private String vatNumber; // TVA intracommunautaire
    
    @Column(precision = 10, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(nullable = false)
    private Integer paymentTermDays = 30; // Délai de paiement en jours
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerType type = CustomerType.COMPANY;
    
    @Column(length = 500)
    private String notes;
    
    // Relations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
    
    // Audit JPA
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(length = 100)
    private String updatedBy;
    
    // Enums
    public enum CustomerStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        SUSPENDED("Suspendu"),
        ARCHIVED("Archivé");
        
        private final String displayName;
        
        CustomerStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum CustomerType {
        COMPANY("Société"),
        INDIVIDUAL("Particulier"),
        ADMINISTRATION("Administration");
        
        private final String displayName;
        
        CustomerType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    // Relation avec les contacts (ajout pour CRM)
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("isPrimary DESC, lastName ASC, firstName ASC")
    private List<com.example.gestioncommerciale.model.crm.Contact> contacts = new java.util.ArrayList<>();

    // Méthodes utilitaires pour contacts
    public void addContact(com.example.gestioncommerciale.model.crm.Contact contact) {
        contacts.add(contact);
        contact.setCustomer(this);
    }

    public void removeContact(com.example.gestioncommerciale.model.crm.Contact contact) {
        contacts.remove(contact);
        contact.setCustomer(null);
    }

    public com.example.gestioncommerciale.model.crm.Contact getPrimaryContact() {
        return contacts.stream()
            .filter(com.example.gestioncommerciale.model.crm.Contact::getIsPrimary)
            .findFirst()
            .orElse(contacts.isEmpty() ? null : contacts.get(0));
    }

    public long getActiveContactsCount() {
        return contacts.stream()
            .filter(contact -> contact.getStatus() == com.example.gestioncommerciale.model.crm.Contact.ContactStatus.ACTIVE)
            .count();
    }
}
