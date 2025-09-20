/*
 * @path src/test/java/com/example/gestioncommerciale/integration/ProductIntegrationTest.java
 * @description Test d'intégration CRUD pour l'API Product avec TestRestTemplate
 */
package com.example.gestioncommerciale.integration;

import com.example.gestioncommerciale.dto.ProductDTO;
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
public class ProductIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void testCreateReadUpdateDelete() {
        // À implémenter : POST, GET all, GET by id, PUT, DELETE assertions
    }
}
