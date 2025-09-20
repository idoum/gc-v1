/*
 * @path src/main/java/com/example/gestioncommerciale/dto/AddressDTO.java
 * @description DTO Address avec validation des champs
 */
package com.example.gestioncommerciale.dto;

import com.example.gestioncommerciale.model.Address;
import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class AddressDTO {
    
    private Long id;
    
    @NotNull(message = "Type d'adresse requis")
    private Address.AddressType type;
    
    @Size(max = 150)
    private String label;
    
    @NotBlank(message = "Adresse requise")
    @Size(max = 150)
    private String street1;
    
    @Size(max = 150)
    private String street2;
    
    @NotBlank(message = "Code postal requis")
    @Pattern(regexp = "\\d{5}")
    private String zipCode;
    
    @NotBlank(message = "Ville requise")
    @Size(max = 100)
    private String city;
    
    @Size(max = 100)
    private String state;
    
    @NotBlank(message = "Code pays requis")
    @Pattern(regexp = "[A-Z]{2}")
    private String countryCode = "FR";
    
    private Boolean isDefault = false;
    
    private Boolean active = true;
    
    @Size(max = 100)
    private String contactName;
    
    @Size(max = 20)
    private String contactPhone;
    
    @Email
    @Size(max = 150)
    private String contactEmail;
    
    @Size(max = 500)
    private String deliveryInstructions;
    
    private Long customerId;
}
