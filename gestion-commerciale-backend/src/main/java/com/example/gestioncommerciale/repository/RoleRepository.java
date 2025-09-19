/*
 * @path src/main/java/com/example/gestioncommerciale/repository/RoleRepository.java
 * @description Repository JPA pour les rôles
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
