/*
 * @path src/test/java/com/example/gestioncommerciale/integration/RoleIntegrationTest.java
 * @description Test d'intégration CRUD pour l'API Role avec TestRestTemplate
 */
package com.example.gestioncommerciale.integration;

import com.example.gestioncommerciale.dto.RoleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void testCreateReadUpdateDelete() {
        // À implémenter : POST, GET all, GET by id, PUT, DELETE assertions
    }
}
