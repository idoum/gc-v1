/*
 * @path src/main/java/com/example/gestioncommerciale/dto/crm/ContactDTO.java
 * @description DTO Contact avec validation complète et informations client
 */
package com.example.gestioncommerciale.dto.crm;

import com.example.gestioncommerciale.model.crm.Contact;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContactDTO {
    
    private Long id;
    
    @Size(max = 20)
    private String code;
    
    private Contact.ContactType type = Contact.ContactType.CONTACT;
    
    private Contact.ContactStatus status = Contact.ContactStatus.ACTIVE;
    
    // Informations personnelles
    private Contact.Civility civility;
    
    @NotBlank(message = "Le prénom est requis")
    @Size(max = 100)
    private String firstName;
    
    @NotBlank(message = "Le nom est requis")
    @Size(max = 100)
    private String lastName;
    
    @Size(max = 100)
    private String jobTitle;
    
    @Size(max = 100)
    private String department;
    
    // Informations de contact
    @Email(message = "L'email doit être valide")
    @Size(max = 150)
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{0,20}$", message = "Numéro de téléphone invalide")
    @Size(max = 20)
    private String phone;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{0,20}$", message = "Numéro de mobile invalide")
    @Size(max = 20)
    private String mobile;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{0,20}$", message = "Numéro de fax invalide")
    @Size(max = 20)
    private String fax;
    
    @Size(max = 200)
    private String website;
    
    // Adresse
    @Size(max = 200)
    private String addressLine1;
    
    @Size(max = 200)
    private String addressLine2;
    
    @Size(max = 100)
    private String city;
    
    @Pattern(regexp = "^[0-9A-Z\\s\\-]{0,20}$", message = "Code postal invalide")
    @Size(max = 20)
    private String postalCode;
    
    @Size(max = 100)
    private String state;
    
    @Size(max = 100)
    private String country;
    
    // Informations complémentaires
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;
    
    @Size(max = 1000)
    private String notes;
    
    @Size(max = 500)
    private String avatarUrl;
    
    // Préférences de communication
    private Boolean emailOptIn = true;
    private Boolean smsOptIn = true;
    private Boolean phoneOptIn = true;
    
    private Contact.PreferredLanguage preferredLanguage = Contact.PreferredLanguage.FRENCH;
    private Contact.PreferredContact preferredContact = Contact.PreferredContact.EMAIL;
    
    // Relation avec le client
    @NotNull(message = "Le client est requis")
    private Long customerId;
    private String customerName;
    private String customerCode;
    
    // Informations de priorité et importance
    private Contact.ContactPriority priority = Contact.ContactPriority.NORMAL;
    
    private Boolean isPrimary = false;
    private Boolean isDecisionMaker = false;
    private Boolean isInfluencer = false;
    
    // Informations de dernière interaction
    private LocalDateTime lastContactDate;
    
    @Future(message = "La prochaine date de contact doit être future")
    private LocalDateTime nextContactDate;
    
    @Size(max = 500)
    private String lastContactNote;
    
    // Champs calculés (read-only)
    private String fullName;
    private String displayName;
    private String formattedAddress;
    private Boolean hasAddress;
    private Boolean isOverdue;
    private Long daysSinceLastContact;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Méthodes de calcul (appelées par le mapper)
    public void calculateFields() {
        // Nom complet
        StringBuilder fullNameBuilder = new StringBuilder();
        if (civility != null) {
            fullNameBuilder.append(getCivilityDisplay()).append(" ");
        }
        fullNameBuilder.append(firstName);
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullNameBuilder.append(" ").append(lastName);
        }
        this.fullName = fullNameBuilder.toString();
        
        // Nom d'affichage
        String displayNameBuilder = fullName;
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            displayNameBuilder += " - " + jobTitle;
        }
        this.displayName = displayNameBuilder;
        
        // Adresse formatée
        this.hasAddress = addressLine1 != null && !addressLine1.trim().isEmpty();
        if (hasAddress) {
            StringBuilder addressBuilder = new StringBuilder();
            addressBuilder.append(addressLine1);
            if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
                addressBuilder.append(", ").append(addressLine2);
            }
            if (postalCode != null && city != null) {
                addressBuilder.append(", ").append(postalCode).append(" ").append(city);
            }
            if (country != null && !country.trim().isEmpty()) {
                addressBuilder.append(", ").append(country);
            }
            this.formattedAddress = addressBuilder.toString();
        }
        
        // Statut de retard
        this.isOverdue = nextContactDate != null && nextContactDate.isBefore(LocalDateTime.now());
        
        // Jours depuis dernier contact
        if (lastContactDate != null) {
            this.daysSinceLastContact = java.time.temporal.ChronoUnit.DAYS.between(
                lastContactDate.toLocalDate(),
                LocalDate.now()
            );
        }
    }
    
    private String getCivilityDisplay() {
        if (civility == null) return "";
        return switch (civility) {
            case MR -> "M.";
            case MRS -> "Mme";
            case MS -> "Mlle";
            case DR -> "Dr";
            case PROF -> "Pr";
        };
    }
}
