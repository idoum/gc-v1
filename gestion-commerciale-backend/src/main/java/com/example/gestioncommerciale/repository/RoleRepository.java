/*
 * @path src/main/java/com/example/gestioncommerciale/repository/RoleRepository.java
 * @description Repository JPA pour les rôles
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> { }
