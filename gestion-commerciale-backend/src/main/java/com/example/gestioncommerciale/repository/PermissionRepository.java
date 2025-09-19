/*
 * @path src/main/java/com/example/gestioncommerciale/repository/PermissionRepository.java
 * @description Repository JPA pour les permissions
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> { }
