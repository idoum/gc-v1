/*
 * @path src/main/java/com/example/gestioncommerciale/controller/LocaleController.java
 * @description Contrôleur pour gérer les URLs localisées /fr/ et /en/ et changement de langue
 */
package com.example.gestioncommerciale.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
@Slf4j
public class LocaleController {

    private final LocaleResolver localeResolver;

    public LocaleController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    /**
     * Pages localisées /fr/ et /en/
     */
    @GetMapping("/{lang}")
    public String localizedHomePage(
            @PathVariable String lang,
            HttpServletRequest request,
            HttpServletResponse response) {
        // Valider la langue
        if (!isValidLanguage(lang)) {
            log.warn("Langue non supportée: {}, redirection vers FR", lang);
            return "redirect:/fr/";
        }

        // Définir la locale en session
        Locale locale = Locale.forLanguageTag(lang);
        localeResolver.setLocale(request, response, locale);

        log.debug("Affichage page d'accueil en langue: {}", lang);
        return "index"; // Retourne le template index.html
    }

    /**
     * Changement de langue avec redirection
     */
    @GetMapping("/change-language")
    public String changeLanguage(
            @RequestParam String lang,
            @RequestParam(defaultValue = "/") String returnUrl,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (isValidLanguage(lang)) {
            Locale locale = Locale.forLanguageTag(lang);
            localeResolver.setLocale(request, response, locale);
            log.info("Changement de langue vers: {}", lang);
        }

        // Rediriger vers l'URL avec la nouvelle langue
        if (returnUrl.startsWith("/fr/") || returnUrl.startsWith("/en/")) {
            returnUrl = "/" + lang + returnUrl.substring(3);
        } else if (!returnUrl.startsWith("/" + lang + "/")) {
            returnUrl = "/" + lang + returnUrl;
        }

        return "redirect:" + returnUrl;
    }

    /**
     * Vérifier si la langue est supportée
     */
    private boolean isValidLanguage(String lang) {
        return "fr".equals(lang) || "en".equals(lang);
    }
}
