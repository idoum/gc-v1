/*
 * @path src/main/java/com/example/gestioncommerciale/repository/catalog/CategoryRepository.java
 * @description Repository Category avec requêtes hiérarchiques
 */
package com.example.gestioncommerciale.repository.catalog;

import com.example.gestioncommerciale.model.catalog.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByCode(String code);
    
    List<Category> findByActiveTrue();
    
    List<Category> findByParentIsNullOrderBySortOrderAscNameAsc();
    
    List<Category> findByParentIdOrderBySortOrderAscNameAsc(Long parentId);
    
    List<Category> findByParentOrderBySortOrderAscNameAsc(Category parent);
    
    @Query("SELECT c FROM Category c WHERE " +
           "(:active IS NULL OR c.active = :active) AND " +
           "(:parentId IS NULL OR c.parent.id = :parentId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "  LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> findWithFilters(
        @Param("active") Boolean active,
        @Param("parentId") Long parentId,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategoriesWithChildren();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    long countProductsByCategory(@Param("category") Category category);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category IN " +
           "(SELECT c FROM Category c WHERE c.parent = :category OR c = :category)")
    long countProductsByCategoryIncludingChildren(@Param("category") Category category);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    boolean existsByParentId(Long parentId);
    
    @Query("SELECT MAX(c.sortOrder) FROM Category c WHERE " +
           "(:parentId IS NULL AND c.parent IS NULL) OR " +
           "(:parentId IS NOT NULL AND c.parent.id = :parentId)")
    Integer findMaxSortOrderByParent(@Param("parentId") Long parentId);
}
