/*
 * @path src/main/java/com/example/gestioncommerciale/service/AuditService.java
 * @description Service pour enregistrer les actions d'audit
 */
package com.example.gestioncommerciale.service;

import com.example.gestioncommerciale.model.AuditLog;
import com.example.gestioncommerciale.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void logAction(String action, String module) {
        logAction(action, module, null, null, null);
    }
    
    public void logAction(String action, String module, String targetType, String targetId, String targetName) {
        try {
            AuditLog log = new AuditLog();
            
            // Utilisateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                log.setUsername(auth.getName());
            }
            
            log.setAction(action);
            log.setModule(module);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setTargetName(targetName);
            
            // Informations de la requête HTTP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setSessionId(request.getSession().getId());
            }
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer l'opération principale
            System.err.println("Erreur lors de l'enregistrement de l'audit : " + e.getMessage());
        }
    }
    
    public void logFailure(String action, String module, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                log.setUsername(auth.getName());
            }
            
            log.setAction(action);
            log.setModule(module);
            log.setStatus(AuditLog.Status.FAILURE);
            log.setErrorMessage(errorMessage);
            
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setSessionId(request.getSession().getId());
            }
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit d'échec : " + e.getMessage());
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}
