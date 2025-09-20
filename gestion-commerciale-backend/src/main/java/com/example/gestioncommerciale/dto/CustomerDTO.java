/*
 * @path src/main/java/com/example/gestioncommerciale/dto/CustomerDTO.java
 * @description DTO Customer avec validation complète
 */
package com.example.gestioncommerciale.dto;

import com.example.gestioncommerciale.model.Customer;
import lombok.Data;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerDTO {
    
    private Long id;
    
    @Size(max = 20)
    private String code;
    
    @NotBlank(message = "Nom de société requis")
    @Size(max = 150)
    private String companyName;
    
    @Size(max = 100)
    private String contactFirstName;
    
    @Size(max = 100)
    private String contactLastName;
    
    @Email(message = "Format d'email invalide")
    @Size(max = 150)
    private String email;
    
    @Size(max = 20)
    private String phone;
    
    @Size(max = 20)
    private String mobile;
    
    @Size(max = 20)
    private String siret;
    
    @Size(max = 15)
    private String vatNumber;
    
    @DecimalMin(value = "0.0")
    private BigDecimal creditLimit;
    
    @Min(value = 0)
    @Max(value = 365)
    private Integer paymentTermDays = 30;
    
    private Customer.CustomerStatus status = Customer.CustomerStatus.ACTIVE;
    
    private Customer.CustomerType type = Customer.CustomerType.COMPANY;
    
    @Size(max = 500)
    private String notes;
    
    // Relations
    @Valid
    private List<AddressDTO> addresses;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
