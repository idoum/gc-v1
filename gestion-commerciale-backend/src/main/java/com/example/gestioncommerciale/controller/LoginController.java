/*
 * @path src/main/java/com/example/gestioncommerciale/controller/LoginController.java
 * @description Contrôleur pour afficher les vues de login et d’erreur
 */
package com.example.gestioncommerciale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Nom d’utilisateur ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Vous avez été déconnecté avec succès.");
        }
        return "login"; // plus "security/login"
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "security/access-denied";
    }
}
