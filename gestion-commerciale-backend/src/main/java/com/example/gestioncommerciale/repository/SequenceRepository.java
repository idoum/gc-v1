/*
 * @path src/main/java/com/example/gestioncommerciale/repository/SequenceRepository.java
 * @description Repository Sequence pour numérotation
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    
    Optional<Sequence> findByTypeAndYear(String type, Integer year);
}
