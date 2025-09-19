/*
 * @path src/main/java/com/example/gestioncommerciale/model/Permission.java
 * @description Entité JPA représentant une permission (module, action, resource)
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String module;
    private String action;
    private String resource;
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
    // getters et setters...
}
