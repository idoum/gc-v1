/*
 * @path src/main/java/com/example/gestioncommerciale/dto/catalog/CategoryDTO.java
 * @description DTO Category avec validation et structure hiérarchique
 */
package com.example.gestioncommerciale.dto.catalog;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryDTO {
    
    private Long id;
    
    @Size(max = 20)
    private String code;
    
    @NotBlank(message = "Nom de catégorie requis")
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    private Boolean active = true;
    
    @Min(value = 0)
    private Integer sortOrder = 0;
    
    @Size(max = 255)
    private String imageUrl;
    
    @Size(max = 255)
    private String iconClass;
    
    // Relations hiérarchiques
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    
    // Statistiques
    private Long productCount = 0L;
    private Integer level = 0;
    private String fullPath;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Méthodes utilitaires
    public boolean isRoot() {
        return parentId == null;
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
