/*
 * @path src/main/java/com/example/gestioncommerciale/dto/catalog/ProductDTO.java
 * @description DTO Product avec validation complète et calculs de prix
 */
package com.example.gestioncommerciale.dto.catalog;

import com.example.gestioncommerciale.model.catalog.Product;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    
    private Long id;
    
    @Size(max = 30)
    private String code;
    
    @NotBlank(message = "Nom du produit requis")
    @Size(max = 150)
    private String name;
    
    @Size(max = 1000)
    private String description;
    
    private Boolean active = true;
    
    private Product.ProductType type = Product.ProductType.PRODUCT;
    
    private Product.ProductStatus status = Product.ProductStatus.AVAILABLE;
    
    // Référence et codes
    @Size(max = 50)
    private String reference;
    
    @Size(max = 50)
    private String sku;
    
    @Size(max = 20)
    private String ean;
    
    // Prix
    @NotNull(message = "Prix unitaire requis")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0")
    private BigDecimal costPrice = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal vatRate = new BigDecimal("20.00");
    
    // Prix calculés (read-only)
    private BigDecimal priceWithVat;
    private BigDecimal vatAmount;
    
    // Stock
    private Boolean stockManaged = false;
    
    @Min(value = 0)
    private Integer stockQuantity = 0;
    
    @Min(value = 0)
    private Integer minStockLevel = 0;
    
    @Min(value = 0)
    private Integer maxStockLevel = 0;
    
    // Stock calculés (read-only)
    private Boolean lowStock;
    private Boolean outOfStock;
    
    // Unités
    @Size(max = 10)
    private String unit = "pce";
    
    @DecimalMin(value = "0.0")
    private BigDecimal weight;
    
    @Size(max = 10)
    private String weightUnit = "kg";
    
    // Dimensions
    @DecimalMin(value = "0.0")
    private BigDecimal length;
    
    @DecimalMin(value = "0.0")
    private BigDecimal width;
    
    @DecimalMin(value = "0.0")
    private BigDecimal height;
    
    @Size(max = 10)
    private String dimensionUnit = "cm";
    
    // Images et documents
    @Size(max = 500)
    private String imageUrl;
    
    @Size(max = 500)
    private String thumbnailUrl;
    
    @Size(max = 500)
    private String documentUrl;
    
    // Relations
    @NotNull(message = "Catégorie requise")
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Méthodes de calcul (appelées par le mapper)
    public void calculateFields() {
        if (unitPrice != null && vatRate != null) {
            BigDecimal vatMultiplier = BigDecimal.ONE.add(vatRate.divide(new BigDecimal("100")));
            this.priceWithVat = unitPrice.multiply(vatMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.vatAmount = unitPrice.multiply(vatRate.divide(new BigDecimal("100"))).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        
        if (stockManaged != null && stockQuantity != null && minStockLevel != null) {
            this.lowStock = stockManaged && stockQuantity <= minStockLevel;
            this.outOfStock = stockManaged && stockQuantity <= 0;
        }
    }
}
