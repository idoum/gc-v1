/*
 * @path src/main/java/com/example/gestioncommerciale/controller/rest/UserController.java
 * @description Contrôleur REST pour gérer les utilisateurs (CRUD)
 */
package com.example.gestioncommerciale.controller.rest;

import com.example.gestioncommerciale.model.User;
import com.example.gestioncommerciale.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<User> list() {
        return repo.findAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return repo.save(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        return repo.findById(id)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User u) {
        return repo.findById(id)
                   .map(user -> {
                       user.setUsername(u.getUsername());
                       user.setPassword(u.getPassword());
                       user.setEmail(u.getEmail());
                       user.setActive(u.getActive());
                       user.setRoles(u.getRoles());
                       return ResponseEntity.ok(repo.save(user));
                   })
                   .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id)
                   .map(user -> { repo.delete(user); return ResponseEntity.noContent().build(); })
                   .orElse(ResponseEntity.notFound().build());
    }
}
