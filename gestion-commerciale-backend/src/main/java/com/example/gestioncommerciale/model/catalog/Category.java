/*
 * @path src/main/java/com/example/gestioncommerciale/model/catalog/Category.java
 * @description Entité Category pour organisation hiérarchique des produits
 */
package com.example.gestioncommerciale.model.catalog;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@EqualsAndHashCode(exclude = {"parent", "children", "products"})
@ToString(exclude = {"parent", "children", "products"})
@EntityListeners(AuditingEntityListener.class)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20, unique = true)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Integer sortOrder = 0;
    
    @Column(length = 255)
    private String imageUrl;
    
    @Column(length = 255)
    private String iconClass;
    
    // Relations hiérarchiques
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, name ASC")
    private List<Category> children;
    
    // Relations avec produits
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
    
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
    
    // Méthodes utilitaires
    public boolean isRoot() {
        return parent == null;
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
    
    public int getLevel() {
        if (parent == null) return 0;
        return parent.getLevel() + 1;
    }
    
    public String getFullPath() {
        if (parent == null) return name;
        return parent.getFullPath() + " > " + name;
    }
}
