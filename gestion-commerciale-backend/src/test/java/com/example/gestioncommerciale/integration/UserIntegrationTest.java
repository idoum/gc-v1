/*
 * @path src/test/java/com/example/gestioncommerciale/integration/UserIntegrationTest.java
 * @description Test d'intégration CRUD pour l'API User avec TestRestTemplate
 */
package com.example.gestioncommerciale.integration;

import com.example.gestioncommerciale.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void testCreateReadUpdateDelete() {
        // À implémenter : POST, GET all, GET by id, PUT, DELETE assertions
    }
}
