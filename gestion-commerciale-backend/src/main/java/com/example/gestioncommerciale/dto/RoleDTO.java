/*
 * @path src/main/java/com/example/gestioncommerciale/dto/RoleDTO.java
 * @description DTO complet exposant les champs de Role pour l'API REST
 */
package com.example.gestioncommerciale.dto;

import java.util.List;

public class RoleDTO {
    private Long id;
    private String name;
    private List<PermissionDTO> permissions;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<PermissionDTO> getPermissions() { return permissions; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPermissions(List<PermissionDTO> permissions) { this.permissions = permissions; }
}
