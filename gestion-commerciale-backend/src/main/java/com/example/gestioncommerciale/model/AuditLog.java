/*
 * @path src/main/java/com/example/gestioncommerciale/model/AuditLog.java
 * @description Enregistrement des actions critiques (connexion, CRUD)
 */
package com.example.gestioncommerciale.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String action; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, EXPORT
    
    @Column(nullable = false)
    private String module; // SECURITY, CATALOGUE, CRM, VENTES, FACTURATION
    
    @Column(name = "target_type")
    private String targetType; // User, Product, Customer, Order, Invoice
    
    @Column(name = "target_id")
    private String targetId;
    
    @Column(name = "target_name")
    private String targetName;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", length = 1000)
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    private Status status; // SUCCESS, FAILURE, ERROR
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "old_values", length = 2000)
    private String oldValues; // JSON des anciennes valeurs
    
    @Column(name = "new_values", length = 2000)
    private String newValues; // JSON des nouvelles valeurs
    
    public enum Status {
        SUCCESS, FAILURE, ERROR
    }
    
    // Constructeurs
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.status = Status.SUCCESS;
    }
    
    public AuditLog(String username, String action, String module) {
        this();
        this.username = username;
        this.action = action;
        this.module = module;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }
    
    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }
}
