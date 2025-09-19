/*
 * @path src/main/java/com/example/gestioncommerciale/repository/PermissionRepository.java
 * @description Repository JPA pour les permissions
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.security.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByModule(String module);
    List<Permission> findByModuleAndAction(String module, String action);
    boolean existsByName(String name);
}
