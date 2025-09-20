/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/catalog/ProductMapper.java
 * @description Mapper Product avec MapStruct et calculs automatiques
 */
package com.example.gestioncommerciale.mapper.catalog;

import com.example.gestioncommerciale.dto.catalog.ProductDTO;
import com.example.gestioncommerciale.model.catalog.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryPath", source = "category.fullPath")
    @Mapping(target = "priceWithVat", expression = "java(product.getPriceWithVat())")
    @Mapping(target = "vatAmount", expression = "java(product.getVatAmount())")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    @Mapping(target = "outOfStock", expression = "java(product.isOutOfStock())")
    ProductDTO toDTO(Product product);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductDTO productDTO);
    
    List<ProductDTO> toDTOList(List<Product> products);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProductFromDTO(ProductDTO dto, @MappingTarget Product entity);
    
    @AfterMapping
    default void calculateFields(@MappingTarget ProductDTO dto) {
        dto.calculateFields();
    }
}
