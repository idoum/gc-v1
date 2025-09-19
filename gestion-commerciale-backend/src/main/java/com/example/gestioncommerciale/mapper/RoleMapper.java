/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/RoleMapper.java
 * @description MapStruct mapper pour transformer Role ↔ RoleDTO
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import com.example.gestioncommerciale.dto.RoleDTO;
import com.example.gestioncommerciale.model.Role;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleDTO toDto(Role entity);
    Role toEntity(RoleDTO dto);
}
