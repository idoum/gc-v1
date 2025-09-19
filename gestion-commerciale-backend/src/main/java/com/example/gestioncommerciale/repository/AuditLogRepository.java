/*
 * @path src/main/java/com/example/gestioncommerciale/repository/AuditLogRepository.java
 * @description Repository JPA pour les logs d'audit
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    
    List<AuditLog> findByModuleOrderByTimestampDesc(String module);
    
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findUserActivityBetween(
        @Param("username") String username, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end);
}
