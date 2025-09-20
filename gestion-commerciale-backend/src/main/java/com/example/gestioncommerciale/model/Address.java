/*
 * @path src/main/java/com/example/gestioncommerciale/model/Address.java
 * @description Entité Address pour gestion des adresses client (facturation, livraison)
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "addresses",
       indexes = {
           @Index(name = "idx_address_customer", columnList = "customer_id"),
           @Index(name = "idx_address_type", columnList = "type"),
           @Index(name = "idx_address_default", columnList = "customer_id, isDefault"),
           @Index(name = "idx_address_country", columnList = "countryCode")
       })
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"customer"})
@ToString(exclude = {"customer"})
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AddressType type;
    
    @Column(length = 150)
    private String label; // Libellé personnalisé (ex: "Siège social", "Entrepôt Nord")
    
    @Column(nullable = false, length = 150)
    private String street1;
    
    @Column(length = 150)
    private String street2;
    
    @Column(nullable = false, length = 10)
    private String zipCode;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(nullable = false, length = 2)
    private String countryCode; // Code pays ISO 3166-1 alpha-2
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    // Contact spécifique à cette adresse (optionnel)
    @Column(length = 100)
    private String contactName;
    
    @Column(length = 20)
    private String contactPhone;
    
    @Column(length = 150)
    private String contactEmail;
    
    // Instructions de livraison/accès
    @Column(length = 500)
    private String deliveryInstructions;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Enums
    public enum AddressType {
        BILLING("Facturation"),
        SHIPPING("Livraison"),
        BOTH("Facturation & Livraison"),
        OTHER("Autre");
        
        private final String displayName;
        
        AddressType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
