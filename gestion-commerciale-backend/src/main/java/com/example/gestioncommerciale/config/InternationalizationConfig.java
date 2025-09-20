/*
 * @path src/main/java/com/example/gestioncommerciale/config/InternationalizationConfig.java
 * @description Configuration Spring pour l'internationalisation FR/EN avec support des URLs localisées
 */
package com.example.gestioncommerciale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {
    
    /**
     * LocaleResolver pour gérer les langues en session
     * Ordre de priorité : URL > paramètre lang > Accept-Language > défaut FR
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.FRENCH); // Défaut : français
        return resolver;
    }
    
    /**
     * Intercepteur pour changer la langue via paramètre ?lang=en ou ?lang=fr
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    /**
     * Enregistrer l'intercepteur de changement de langue
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
