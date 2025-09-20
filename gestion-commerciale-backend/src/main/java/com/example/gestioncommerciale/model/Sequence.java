/*
 * @path src/main/java/com/example/gestioncommerciale/model/Sequence.java
 * @description Entité pour la numérotation séquentielle
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sequences")
@Data
public class Sequence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Long currentValue = 0L;
    
    @Column(length = 10)
    private String prefix;
    
    @Column(nullable = false)
    private Integer paddingLength = 4;
    
    public Sequence() {}
    
    public Sequence(String type, Integer year, String prefix) {
        this.type = type;
        this.year = year;
        this.prefix = prefix;
    }
}
