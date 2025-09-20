/*
 * @path src/main/java/com/example/gestioncommerciale/model/crm/Contact.java
 * @description Entité Contact pour gestion CRM avec relations client et historique
 */
package com.example.gestioncommerciale.model.crm;

import com.example.gestioncommerciale.model.Customer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Data
@EqualsAndHashCode(exclude = {"customer"})
@ToString(exclude = {"customer"})
@EntityListeners(AuditingEntityListener.class)
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20, unique = true)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type = ContactType.CONTACT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status = ContactStatus.ACTIVE;
    
    // Informations personnelles
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Civility civility;
    
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    @Column(length = 100)
    private String jobTitle;
    
    @Column(length = 100)
    private String department;
    
    // Informations de contact
    @Column(length = 150)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 20)
    private String mobile;
    
    @Column(length = 20)
    private String fax;
    
    @Column(length = 200)
    private String website;
    
    // Adresse (optionnelle, peut différer de celle du client)
    @Column(length = 200)
    private String addressLine1;
    
    @Column(length = 200)
    private String addressLine2;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 100)
    private String country;
    
    // Informations complémentaires
    private LocalDate birthDate;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(length = 500)
    private String avatarUrl;
    
    // Préférences de communication
    private Boolean emailOptIn = true;
    private Boolean smsOptIn = true;
    private Boolean phoneOptIn = true;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PreferredLanguage preferredLanguage = PreferredLanguage.FRENCH;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PreferredContact preferredContact = PreferredContact.EMAIL;
    
    // Relation avec le client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Informations de priorité et importance
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactPriority priority = ContactPriority.NORMAL;
    
    private Boolean isPrimary = false;
    private Boolean isDecisionMaker = false;
    private Boolean isInfluencer = false;
    
    // Informations de dernière interaction
    private LocalDateTime lastContactDate;
    private LocalDateTime nextContactDate;
    
    @Column(length = 500)
    private String lastContactNote;
    
    // Audit
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
    public enum ContactType {
        CONTACT, LEAD, PROSPECT, PARTNER, SUPPLIER, INTERNAL
    }
    
    public enum ContactStatus {
        ACTIVE, INACTIVE, BLOCKED, ARCHIVED, BOUNCED
    }
    
    public enum Civility {
        MR, MRS, MS, DR, PROF
    }
    
    public enum PreferredLanguage {
        FRENCH, ENGLISH, SPANISH, GERMAN, ITALIAN
    }
    
    public enum PreferredContact {
        EMAIL, PHONE, MOBILE, SMS, MAIL, NONE
    }
    
    public enum ContactPriority {
        LOW, NORMAL, HIGH, CRITICAL
    }
    
    // Méthodes utilitaires
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (civility != null) {
            fullName.append(getCivilityDisplay()).append(" ");
        }
        fullName.append(firstName);
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullName.append(" ").append(lastName);
        }
        return fullName.toString();
    }
    
    public String getCivilityDisplay() {
        if (civility == null) return "";
        return switch (civility) {
            case MR -> "M.";
            case MRS -> "Mme";
            case MS -> "Mlle";
            case DR -> "Dr";
            case PROF -> "Pr";
        };
    }
    
    public String getDisplayName() {
        String fullName = getFullName();
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            fullName += " - " + jobTitle;
        }
        return fullName;
    }
    
    public boolean hasAddress() {
        return addressLine1 != null && !addressLine1.trim().isEmpty();
    }
    
    public String getFormattedAddress() {
        if (!hasAddress()) return "";
        
        StringBuilder address = new StringBuilder();
        address.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }
        if (postalCode != null && city != null) {
            address.append(", ").append(postalCode).append(" ").append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            address.append(", ").append(country);
        }
        return address.toString();
    }
    
    public boolean isOverdue() {
        return nextContactDate != null && nextContactDate.isBefore(LocalDateTime.now());
    }
    
    public long getDaysSinceLastContact() {
        if (lastContactDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(
            lastContactDate.toLocalDate(), 
            LocalDate.now()
        );
    }
}
