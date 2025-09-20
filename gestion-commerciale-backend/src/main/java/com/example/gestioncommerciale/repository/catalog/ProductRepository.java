/*
 * @path src/main/java/com/example/gestioncommerciale/repository/catalog/ProductRepository.java
 * @description Repository Product avec requêtes de recherche avancées
 */
package com.example.gestioncommerciale.repository.catalog;

import com.example.gestioncommerciale.model.catalog.Product;
import com.example.gestioncommerciale.model.catalog.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByCode(String code);
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByEan(String ean);
    
    List<Product> findByActiveTrue();
    
    List<Product> findByCategoryOrderByNameAsc(Category category);
    
    List<Product> findByCategoryIdOrderByNameAsc(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:minPrice IS NULL OR p.unitPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.unitPrice <= :maxPrice) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "  LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(p.ean) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findWithFilters(
        @Param("active") Boolean active,
        @Param("categoryId") Long categoryId,
        @Param("status") Product.ProductStatus status,
        @Param("type") Product.ProductType type,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Product p WHERE p.stockManaged = true AND p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.stockManaged = true AND p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    long countByCategory(@Param("category") Category category);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockManaged = true AND p.stockQuantity <= p.minStockLevel")
    long countLowStockProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockManaged = true AND p.stockQuantity = 0")
    long countOutOfStockProducts();
    
    boolean existsByCode(String code);
    
    boolean existsBySku(String sku);
    
    boolean existsByEan(String ean);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    boolean existsBySkuAndIdNot(String sku, Long id);
    
    boolean existsByEanAndIdNot(String ean, Long id);
}
