/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/ProductMapper.java
 * @description MapStruct mapper pour transformer Product ↔ ProductDTO
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import com.example.gestioncommerciale.dto.ProductDTO;
import com.example.gestioncommerciale.model.Product;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
    ProductDTO toDto(Product entity);
    Product toEntity(ProductDTO dto);
}
