/*
 * @path src/main/java/com/example/gestioncommerciale/service/DataSeedService.java
 * @description Service pour initialiser des données de test supplémentaires
 */
package com.example.gestioncommerciale.service;

import com.example.gestioncommerciale.model.AuditLog;
import com.example.gestioncommerciale.model.security.Permission;
import com.example.gestioncommerciale.model.security.Role;
import com.example.gestioncommerciale.model.security.User;
import com.example.gestioncommerciale.repository.AuditLogRepository;
import com.example.gestioncommerciale.repository.PermissionRepository;
import com.example.gestioncommerciale.repository.RoleRepository;
import com.example.gestioncommerciale.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Profile("dev") // Seulement en mode développement
public class DataSeedService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si les données existent déjà
        if (userRepository.count() > 0) {
            System.out.println("Les données de test existent déjà.");
            return;
        }

        System.out.println("Initialisation des données de test...");
        seedAdditionalTestData();
        System.out.println("Données de test initialisées avec succès!");
    }

    private void seedAdditionalTestData() {
        // Créer des utilisateurs de test supplémentaires
        createAdditionalUsers();
        
        // Créer des logs d'audit de test
        createTestAuditLogs();
    }

    private void createAdditionalUsers() {
        List<User> testUsers = Arrays.asList(
            createUser("vendeur1", "vendeur123", "vendeur1@test.com", "Paul", "Vendeur"),
            createUser("vendeur2", "vendeur123", "vendeur2@test.com", "Alice", "Vendeuse"),
            createUser("support", "support123", "support@test.com", "Marc", "Support"),
            createUser("stagiaire", "stage123", "stagiaire@test.com", "Julie", "Stagiaire")
        );

        Role userRole = roleRepository.findByName("USER").orElse(null);
        
        for (User user : testUsers) {
            if (!userRepository.existsByUsername(user.getUsername())) {
                User savedUser = userRepository.save(user);
                if (userRole != null) {
                    savedUser.addRole(userRole);
                    userRepository.save(savedUser);
                }
            }
        }
    }

    private User createUser(String username, String password, String email, 
                           String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        return user;
    }

    private void createTestAuditLogs() {
        List<String> usernames = Arrays.asList("admin", "manager", "user", "vendeur1", "vendeur2");
        List<String> actions = Arrays.asList("LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "READ");
        List<String> modules = Arrays.asList("SECURITY", "CATALOGUE", "CRM", "VENTES", "FACTURATION");
        
        // Générer 50 logs de test sur les 7 derniers jours
        for (int i = 0; i < 50; i++) {
            AuditLog log = new AuditLog();
            log.setUsername(usernames.get((int) (Math.random() * usernames.size())));
            log.setAction(actions.get((int) (Math.random() * actions.size())));
            log.setModule(modules.get((int) (Math.random() * modules.size())));
            log.setTimestamp(LocalDateTime.now().minusDays((long) (Math.random() * 7)));
            log.setIpAddress("192.168.1." + (100 + (int) (Math.random() * 50)));
            log.setStatus(Math.random() > 0.1 ? AuditLog.Status.SUCCESS : AuditLog.Status.FAILURE);
            
            if (log.getStatus() == AuditLog.Status.FAILURE) {
                log.setErrorMessage("Erreur de test #" + i);
            }
            
            auditLogRepository.save(log);
        }
    }
}
