/*
 * @path src/main/java/com/example/gestioncommerciale/dto/UserDTO.java
 * @description DTO complet exposant les champs de User pour l'API REST (sans mot de passe)
 */
package com.example.gestioncommerciale.dto;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean active;
    private List<RoleDTO> roles;

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Boolean getActive() { return active; }
    public List<RoleDTO> getRoles() { return roles; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setActive(Boolean active) { this.active = active; }
    public void setRoles(List<RoleDTO> roles) { this.roles = roles; }
}
