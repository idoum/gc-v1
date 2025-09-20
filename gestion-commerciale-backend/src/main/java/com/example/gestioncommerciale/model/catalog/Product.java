/*
 * @path src/main/java/com/example/gestioncommerciale/model/catalog/Product.java
 * @description Entité Product pour gestion du catalogue avec prix et stock
 */
package com.example.gestioncommerciale.model.catalog;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(exclude = {"category"})
@ToString(exclude = {"category"})
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 30, unique = true)
    private String code;
    
    @Column(nullable = false, length = 150)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type = ProductType.PRODUCT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;
    
    // Référence et codes
    @Column(length = 50)
    private String reference;
    
    @Column(length = 50)
    private String sku;
    
    @Column(length = 20)
    private String ean;
    
    // Prix
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal vatRate = new BigDecimal("20.00");
    
    // Stock
    @Column(nullable = false)
    private Boolean stockManaged = false;
    
    @Column(nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(nullable = false)
    private Integer minStockLevel = 0;
    
    @Column(nullable = false)
    private Integer maxStockLevel = 0;
    
    // Unités
    @Column(length = 10)
    private String unit = "pce";
    
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;
    
    @Column(length = 10)
    private String weightUnit = "kg";
    
    // Dimensions
    @Column(precision = 8, scale = 2)
    private BigDecimal length;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal width;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal height;
    
    @Column(length = 10)
    private String dimensionUnit = "cm";
    
    // Images et documents
    @Column(length = 500)
    private String imageUrl;
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @Column(length = 500)
    private String documentUrl;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
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
    public enum ProductType {
        PRODUCT, SERVICE, VARIANT, BUNDLE
    }
    
    public enum ProductStatus {
        AVAILABLE, OUT_OF_STOCK, DISCONTINUED, PENDING, DRAFT
    }
    
    // Méthodes utilitaires
    public BigDecimal getPriceWithVat() {
        if (unitPrice == null || vatRate == null) {
            return unitPrice != null ? unitPrice : BigDecimal.ZERO;
        }
        BigDecimal vatMultiplier = BigDecimal.ONE.add(vatRate.divide(new BigDecimal("100")));
        return unitPrice.multiply(vatMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getVatAmount() {
        if (unitPrice == null || vatRate == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(vatRate.divide(new BigDecimal("100"))).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isLowStock() {
        return stockManaged && stockQuantity <= minStockLevel;
    }
    
    public boolean isOutOfStock() {
        return stockManaged && stockQuantity <= 0;
    }
}
