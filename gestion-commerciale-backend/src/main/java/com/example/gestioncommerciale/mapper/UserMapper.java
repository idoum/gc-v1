/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/UserMapper.java
 * @description MapStruct mapper pour transformer User ↔ UserDTO (sans password)
 */
package com.example.gestioncommerciale.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.gestioncommerciale.dto.UserDTO;
import com.example.gestioncommerciale.model.User;

@Mapper(componentModel = "spring", uses = { RoleMapper.class })
public interface UserMapper {
    UserDTO toDto(User entity);

    @Mapping(target = "password", ignore = true)
    User toEntity(UserDTO dto);
}
