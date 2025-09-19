/*
 * @path src/test/java/com/example/gestioncommerciale/integration/PermissionIntegrationTest.java
 * @description Test d'intégration CRUD complet pour l'API Permission avec assertions
 */
package com.example.gestioncommerciale.integration;

import com.example.gestioncommerciale.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PermissionIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void testCreateReadUpdateDelete() {
        // Setup Basic Auth
        rest = rest.withBasicAuth("admin", "admin123");

        // 1. CREATE - POST /api/permissions
        PermissionDTO newPermission = new PermissionDTO();
        newPermission.setName("TEST_PERMISSION");
        newPermission.setModule("TEST");
        newPermission.setAction("CREATE");
        newPermission.setResource("ALL");

        ResponseEntity<PermissionDTO> createResponse = rest.postForEntity(
            "/api/permissions", newPermission, PermissionDTO.class);
        
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        Long createdId = createResponse.getBody().getId();
        assertNotNull(createdId);

        // 2. READ - GET /api/permissions/{id}
        ResponseEntity<PermissionDTO> getResponse = rest.getForEntity(
            "/api/permissions/" + createdId, PermissionDTO.class);
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("TEST_PERMISSION", getResponse.getBody().getName());

        // 3. UPDATE - PUT /api/permissions/{id}
        PermissionDTO updatePermission = getResponse.getBody();
        updatePermission.setName("UPDATED_PERMISSION");

        HttpEntity<PermissionDTO> updateEntity = new HttpEntity<>(updatePermission);
        ResponseEntity<PermissionDTO> updateResponse = rest.exchange(
            "/api/permissions/" + createdId, HttpMethod.PUT, updateEntity, PermissionDTO.class);
        
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("UPDATED_PERMISSION", updateResponse.getBody().getName());

        // 4. DELETE - DELETE /api/permissions/{id}
        ResponseEntity<Void> deleteResponse = rest.exchange(
            "/api/permissions/" + createdId, HttpMethod.DELETE, null, Void.class);
        
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 5. Vérifier la suppression - GET /api/permissions/{id}
        ResponseEntity<String> notFoundResponse = rest.getForEntity(
            "/api/permissions/" + createdId, String.class);
        
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
    }

    @Test
    void testGetAllPermissions() {
        rest = rest.withBasicAuth("admin", "admin123");
        
        ResponseEntity<PermissionDTO[]> response = rest.getForEntity(
            "/api/permissions", PermissionDTO[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
    }
}
