/*
 * @path src/main/java/com/example/gestioncommerciale/controller/HomeController.java
 * @description Contrôleur principal pour les pages d'accueil avec support i18n
 */
package com.example.gestioncommerciale.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Locale;

@Controller
@Slf4j
public class HomeController {
    
    /**
     * Page d'accueil localisée
     */
    @GetMapping({"/{lang}", "/{lang}/"})
    public String home(
            @PathVariable String lang,
            Model model,
            Locale locale
    ) {
        // Valider la langue
        if (!"fr".equals(lang) && !"en".equals(lang)) {
            log.warn("Langue non supportée: {}, redirection vers FR", lang);
            return "redirect:/fr/";
        }
        
        log.debug("Affichage page d'accueil en langue: {} (locale: {})", lang, locale);
        
        // Ajouter des données pour la vue
        model.addAttribute("currentLang", lang);
        model.addAttribute("pageTitle", "nav.home");
        
        // Statistiques simulées (remplacer par de vraies données plus tard)
        model.addAttribute("stats", getHomeStats());
        
        return "index";
    }
    
    /**
     * Redirection depuis la racine vers la langue par défaut
     */
    @GetMapping("/")
    public String redirectToDefaultLocale() {
        return "redirect:/fr/";
    }
    
    /**
     * Statistiques pour la page d'accueil (simulées)
     */
    private Object getHomeStats() {
        // Retourner des statistiques simulées
        // À remplacer par de vraies données depuis les services
        return new Object() {
            public final int customers = 1234;
            public final int products = 567;
            public final int orders = 89;
            public final int invoices = 45;
        };
    }
}
