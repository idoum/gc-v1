/*
 * @path src/main/java/com/example/gestioncommerciale/service/catalog/ProductService.java
 * @description Service métier Product avec gestion complète du catalogue
 */
package com.example.gestioncommerciale.service.catalog;

import com.example.gestioncommerciale.dto.catalog.ProductDTO;
import com.example.gestioncommerciale.mapper.catalog.ProductMapper;
import com.example.gestioncommerciale.model.catalog.Category;
import com.example.gestioncommerciale.model.catalog.Product;
import com.example.gestioncommerciale.repository.catalog.CategoryRepository;
import com.example.gestioncommerciale.repository.catalog.ProductRepository;
import com.example.gestioncommerciale.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final SequenceService sequenceService;
    
    public static final String PRODUCT_TYPE = "PRODUCT";
    public static final String PRODUCT_PREFIX = "PRD";
    
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findByCode(String code) {
        return productRepository.findByCode(code)
            .map(productMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findBySku(String sku) {
        return productRepository.findBySku(sku)
            .map(productMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> findActiveProducts() {
        return productRepository.findByActiveTrue()
            .stream()
            .map(productMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdOrderByNameAsc(categoryId)
            .stream()
            .map(productMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<ProductDTO> findWithFilters(
            Boolean active, 
            Long categoryId, 
            Product.ProductStatus status,
            Product.ProductType type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search, 
            Pageable pageable
    ) {
        Page<Product> products = productRepository.findWithFilters(
            active, categoryId, status, type, minPrice, maxPrice, search, pageable
        );
        
        List<ProductDTO> dtos = products.getContent()
            .stream()
            .map(productMapper::toDTO)
            .toList();
        
        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> findLowStockProducts() {
        return productRepository.findLowStockProducts()
            .stream()
            .map(productMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> findOutOfStockProducts() {
        return productRepository.findOutOfStockProducts()
            .stream()
            .map(productMapper::toDTO)
            .toList();
    }
    
    public ProductDTO create(ProductDTO productDTO) {
        if (productDTO.getCode() == null || productDTO.getCode().trim().isEmpty()) {
            productDTO.setCode(sequenceService.generateSequentialCode(PRODUCT_TYPE, PRODUCT_PREFIX));
        }
        
        validateUniqueFields(productDTO, null);
        
        Product product = productMapper.toEntity(productDTO);
        
        // Gérer la relation catégorie
        Category category = categoryRepository.findById(productDTO.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + productDTO.getCategoryId()));
        product.setCategory(category);
        
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        Product saved = productRepository.save(product);
        log.info("Produit créé avec succès: {}", saved.getCode());
        
        return productMapper.toDTO(saved);
    }
    
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        validateUniqueFields(productDTO, id);
        
        // Gérer la relation catégorie
        if (!existing.getCategory().getId().equals(productDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + productDTO.getCategoryId()));
            existing.setCategory(category);
        }
        
        productMapper.updateProductFromDTO(productDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        
        Product updated = productRepository.save(existing);
        log.info("Produit mis à jour avec succès: {}", updated.getCode());
        
        return productMapper.toDTO(updated);
    }
    
    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        // TODO: Vérifier qu'il n'y a pas de commandes/factures liées
        
        productRepository.delete(product);
        log.info("Produit supprimé avec succès: {}", product.getCode());
    }
    
    public ProductDTO updateStock(Long id, Integer newQuantity) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        if (!product.getStockManaged()) {
            throw new IllegalArgumentException("Ce produit n'a pas de gestion de stock activée");
        }
        
        product.setStockQuantity(newQuantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product updated = productRepository.save(product);
        log.info("Stock mis à jour pour le produit {}: {} unités", updated.getCode(), newQuantity);
        
        return productMapper.toDTO(updated);
    }
    
    @Transactional(readOnly = true)
    public long countAll() {
        return productRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countActiveProducts();
    }
    
    @Transactional(readOnly = true)
    public long countLowStockProducts() {
        return productRepository.countLowStockProducts();
    }
    
    @Transactional(readOnly = true)
    public long countOutOfStockProducts() {
        return productRepository.countOutOfStockProducts();
    }
    
    @Transactional(readOnly = true)
    public long countByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    private void validateUniqueFields(ProductDTO productDTO, Long excludeId) {
        if (productDTO.getCode() != null) {
            boolean codeExists = (excludeId == null) 
                ? productRepository.existsByCode(productDTO.getCode())
                : productRepository.existsByCodeAndIdNot(productDTO.getCode(), excludeId);
                
            if (codeExists) {
                throw new IllegalArgumentException("Un produit avec ce code existe déjà: " + productDTO.getCode());
            }
        }
        
        if (productDTO.getSku() != null && !productDTO.getSku().trim().isEmpty()) {
            boolean skuExists = (excludeId == null) 
                ? productRepository.existsBySku(productDTO.getSku())
                : productRepository.existsBySkuAndIdNot(productDTO.getSku(), excludeId);
                
            if (skuExists) {
                throw new IllegalArgumentException("Un produit avec ce SKU existe déjà: " + productDTO.getSku());
            }
        }
        
        if (productDTO.getEan() != null && !productDTO.getEan().trim().isEmpty()) {
            boolean eanExists = (excludeId == null) 
                ? productRepository.existsByEan(productDTO.getEan())
                : productRepository.existsByEanAndIdNot(productDTO.getEan(), excludeId);
                
            if (eanExists) {
                throw new IllegalArgumentException("Un produit avec cet EAN existe déjà: " + productDTO.getEan());
            }
        }
    }
}
