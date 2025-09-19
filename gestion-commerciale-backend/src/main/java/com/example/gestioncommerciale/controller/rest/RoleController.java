/*
 * @path src/main/java/com/example/gestioncommerciale/controller/rest/RoleController.java
 * @description Contrôleur REST pour gérer les rôles (CRUD)
 */
package com.example.gestioncommerciale.controller.rest;

import com.example.gestioncommerciale.model.Role;
import com.example.gestioncommerciale.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository repo;

    public RoleController(RoleRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Role> list() {
        return repo.findAll();
    }

    @PostMapping
    public Role create(@RequestBody Role role) {
        return repo.save(role);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> get(@PathVariable Long id) {
        return repo.findById(id)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role r) {
        return repo.findById(id)
                   .map(role -> {
                       role.setName(r.getName());
                       role.setPermissions(r.getPermissions());
                       return ResponseEntity.ok(repo.save(role));
                   })
                   .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id)
                   .map(role -> { repo.delete(role); return ResponseEntity.noContent().build(); })
                   .orElse(ResponseEntity.notFound().build());
    }
}
