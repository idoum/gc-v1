/*
 * @path src/main/java/com/example/gestioncommerciale/model/security/Permission.java
 * @description Entité JPA représentant une permission métier
 */
package com.example.gestioncommerciale.model.security;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String module; // CATALOGUE, CRM, VENTES, FACTURATION, ADMIN
    
    @Column(nullable = false)
    private String action; // CREATE, READ, UPDATE, DELETE, EXPORT
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
    
    // Constructeurs
    public Permission() {}
    
    public Permission(String name, String description, String module, String action) {
        this.name = name;
        this.description = description;
        this.module = module;
        this.action = action;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
