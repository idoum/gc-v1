/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/CategoryMapper.java
 * @description MapStruct mapper pour transformer Category ↔ CategoryDTO
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import com.example.gestioncommerciale.dto.CategoryDTO;
import com.example.gestioncommerciale.model.catalog.Category;

@Mapper(componentModel = "spring", implementationName = "CategoryDefaultMapperImpl")
public interface CategoryMapper {
    CategoryDTO toDto(Category entity);

    Category toEntity(CategoryDTO dto);
}
