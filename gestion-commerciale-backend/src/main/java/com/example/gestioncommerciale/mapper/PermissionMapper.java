/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/PermissionMapper.java
 * @description MapStruct mapper pour transformer Permission ↔ PermissionDTO
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import com.example.gestioncommerciale.dto.PermissionDTO;
import com.example.gestioncommerciale.model.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO toDto(Permission entity);
    Permission toEntity(PermissionDTO dto);
}
