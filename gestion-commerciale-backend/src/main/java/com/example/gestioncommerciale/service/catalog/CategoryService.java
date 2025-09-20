/*
 * @path src/main/java/com/example/gestioncommerciale/service/catalog/CategoryService.java
 * @description Service métier Category avec gestion hiérarchique
 */
package com.example.gestioncommerciale.service.catalog;

import com.example.gestioncommerciale.dto.catalog.CategoryDTO;
import com.example.gestioncommerciale.mapper.catalog.CategoryMapper;
import com.example.gestioncommerciale.model.catalog.Category;
import com.example.gestioncommerciale.repository.catalog.CategoryRepository;
import com.example.gestioncommerciale.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SequenceService sequenceService;
    
    public static final String CATEGORY_TYPE = "CATEGORY";
    public static final String CATEGORY_PREFIX = "CAT";
    
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findById(Long id) {
        return categoryRepository.findById(id)
            .map(this::enrichCategoryDTO);
    }
    
    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        return categoryRepository.findById(id)
            .map(this::enrichCategoryDTO)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findByCode(String code) {
        return categoryRepository.findByCode(code)
            .map(this::enrichCategoryDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> findRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc()
            .stream()
            .map(this::enrichCategoryDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> findByParent(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(parentId)
            .stream()
            .map(this::enrichCategoryDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> findActiveCategories() {
        return categoryRepository.findByActiveTrue()
            .stream()
            .map(this::enrichCategoryDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findWithFilters(Boolean active, Long parentId, String search, Pageable pageable) {
        Page<Category> categories = categoryRepository.findWithFilters(active, parentId, search, pageable);
        List<CategoryDTO> dtos = categories.getContent()
            .stream()
            .map(this::enrichCategoryDTO)
            .toList();
        
        return new PageImpl<>(dtos, pageable, categories.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDTO> findCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();
        return rootCategories.stream()
            .map(this::enrichCategoryDTOWithChildren)
            .toList();
    }
    
    public CategoryDTO create(CategoryDTO categoryDTO) {
        if (categoryDTO.getCode() == null || categoryDTO.getCode().trim().isEmpty()) {
            categoryDTO.setCode(sequenceService.generateSequentialCode(CATEGORY_TYPE, CATEGORY_PREFIX));
        }
        
        validateUniqueFields(categoryDTO, null);
        
        Category category = categoryMapper.toEntity(categoryDTO);
        
        // Gérer la relation parent
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new RuntimeException("Catégorie parent non trouvée avec ID: " + categoryDTO.getParentId()));
            category.setParent(parent);
        }
        
        // Gérer l'ordre de tri
        if (category.getSortOrder() == null || category.getSortOrder() == 0) {
            Integer maxOrder = categoryRepository.findMaxSortOrderByParent(categoryDTO.getParentId());
            category.setSortOrder((maxOrder != null ? maxOrder : 0) + 10);
        }
        
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        Category saved = categoryRepository.save(category);
        log.info("Catégorie créée avec succès: {}", saved.getCode());
        
        return enrichCategoryDTO(categoryMapper.toDTO(saved));
    }
    
    public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
        Category existing = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
        
        validateUniqueFields(categoryDTO, id);
        
        // Gérer la relation parent
        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(id)) {
                throw new IllegalArgumentException("Une catégorie ne peut pas être son propre parent");
            }
            
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new RuntimeException("Catégorie parent non trouvée avec ID: " + categoryDTO.getParentId()));
            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }
        
        categoryMapper.updateCategoryFromDTO(categoryDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        
        Category updated = categoryRepository.save(existing);
        log.info("Catégorie mise à jour avec succès: {}", updated.getCode());
        
        return enrichCategoryDTO(categoryMapper.toDTO(updated));
    }
    
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
        
        // Vérifier qu'il n'y a pas de catégories enfants
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie qui contient des sous-catégories");
        }
        
        // Vérifier qu'il n'y a pas de produits
        long productCount = categoryRepository.countProductsByCategory(category);
        if (productCount > 0) {
            throw new IllegalArgumentException("Impossible de supprimer une catégorie qui contient des produits");
        }
        
        categoryRepository.delete(category);
        log.info("Catégorie supprimée avec succès: {}", category.getCode());
    }
    
    @Transactional(readOnly = true)
    public long countAll() {
        return categoryRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long countActiveCategories() {
        return categoryRepository.findByActiveTrue().size();
    }
    
    @Transactional(readOnly = true)
    public long countRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc().size();
    }
    
    private void validateUniqueFields(CategoryDTO categoryDTO, Long excludeId) {
        if (categoryDTO.getCode() != null) {
            boolean codeExists = (excludeId == null) 
                ? categoryRepository.existsByCode(categoryDTO.getCode())
                : categoryRepository.existsByCodeAndIdNot(categoryDTO.getCode(), excludeId);
                
            if (codeExists) {
                throw new IllegalArgumentException("Une catégorie avec ce code existe déjà: " + categoryDTO.getCode());
            }
        }
    }
    
    private CategoryDTO enrichCategoryDTO(Category category) {
        CategoryDTO dto = categoryMapper.toDTO(category);
        dto.setProductCount(categoryRepository.countProductsByCategory(category));
        return dto;
    }
    
    private CategoryDTO enrichCategoryDTO(CategoryDTO dto) {
        // Enrichir avec le nombre de produits si nécessaire
        return dto;
    }
    
    private CategoryDTO enrichCategoryDTOWithChildren(Category category) {
        CategoryDTO dto = enrichCategoryDTO(category);
        if (category.hasChildren()) {
            List<CategoryDTO> childrenDTOs = category.getChildren()
                .stream()
                .map(this::enrichCategoryDTOWithChildren)
                .toList();
            dto.setChildren(childrenDTOs);
        }
        return dto;
    }
}
