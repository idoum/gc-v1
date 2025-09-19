/*
 * @path src/main/java/com/example/gestioncommerciale/dto/PermissionDTO.java
 * @description DTO complet exposant les champs de Permission pour l'API REST
 */
package com.example.gestioncommerciale.dto;

public class PermissionDTO {
    private Long id;
    private String name;
    private String module;
    private String action;
    private String resource;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getResource() { return resource; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setModule(String module) { this.module = module; }
    public void setAction(String action) { this.action = action; }
    public void setResource(String resource) { this.resource = resource; }
}
