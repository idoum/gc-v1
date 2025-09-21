/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/catalog/CategoryMapper.java
 * @description Mapper Category avec MapStruct pour structure hiérarchique
 */
package com.example.gestioncommerciale.mapper.catalog;

import com.example.gestioncommerciale.dto.catalog.CategoryDTO;
import com.example.gestioncommerciale.model.catalog.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "CategoryCatalogMapperImpl", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "fullPath", expression = "java(category.getFullPath())")
    @Mapping(target = "level", expression = "java(category.getLevel())")
    @Mapping(target = "productCount", ignore = true) // Sera rempli par le service
    CategoryDTO toDTO(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);

    List<CategoryDTO> toDTOList(List<Category> categories);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCategoryFromDTO(CategoryDTO dto, @MappingTarget Category entity);
}
