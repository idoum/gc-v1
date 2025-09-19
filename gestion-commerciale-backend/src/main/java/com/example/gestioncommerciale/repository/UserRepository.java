/*
 * @path src/main/java/com/example/gestioncommerciale/repository/UserRepository.java
 * @description Repository JPA pour les utilisateurs
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> { }
