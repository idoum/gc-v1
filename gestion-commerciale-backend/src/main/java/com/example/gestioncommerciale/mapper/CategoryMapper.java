/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/CategoryMapper.java
 * @description MapStruct mapper pour transformer Category ↔ CategoryDTO
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import com.example.gestioncommerciale.dto.CategoryDTO;
import com.example.gestioncommerciale.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDto(Category entity);
    Category toEntity(CategoryDTO dto);
}
