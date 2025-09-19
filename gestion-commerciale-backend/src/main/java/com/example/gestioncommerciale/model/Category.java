/*
 * @path src/main/java/com/example/gestioncommerciale/model/Category.java
 * @description Entité JPA pour la table categories (catalogue)
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    // getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
