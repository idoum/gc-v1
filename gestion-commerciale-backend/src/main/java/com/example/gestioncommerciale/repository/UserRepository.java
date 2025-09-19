/*
 * @path src/main/java/com/example/gestioncommerciale/repository/UserRepository.java
 * @description Repository JPA pour les utilisateurs
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
