/*
 * @path src/main/java/com/example/gestioncommerciale/controller/rest/PermissionController.java
 * @description Contrôleur REST pour gérer les permissions (CRUD)
 */
package com.example.gestioncommerciale.controller.rest;

import com.example.gestioncommerciale.model.Permission;
import com.example.gestioncommerciale.repository.PermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionRepository repo;

    public PermissionController(PermissionRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Permission> list() {
        return repo.findAll();
    }

    @PostMapping
    public Permission create(@RequestBody Permission permission) {
        return repo.save(permission);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> get(@PathVariable Long id) {
        return repo.findById(id)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> update(@PathVariable Long id, @RequestBody Permission perm) {
        return repo.findById(id)
                   .map(p -> {
                       p.setName(perm.getName());
                       p.setModule(perm.getModule());
                       p.setAction(perm.getAction());
                       p.setResource(perm.getResource());
                       return ResponseEntity.ok(repo.save(p));
                   })
                   .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id)
                   .map(p -> { repo.delete(p); return ResponseEntity.noContent().build(); })
                   .orElse(ResponseEntity.notFound().build());
    }
}
