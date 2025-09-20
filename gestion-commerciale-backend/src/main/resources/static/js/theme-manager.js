/*
 * @path src/main/resources/static/js/theme-manager.js
 * @description Gestionnaire de thèmes clair/sombre avec persistance localStorage
 */

class ThemeManager {
    constructor() {
        this.themes = {
            LIGHT: 'light',
            DARK: 'dark',
            AUTO: 'auto'
        };
        
        this.storageKey = 'gc-theme-preference';
        this.currentTheme = this.getStoredTheme() || this.themes.AUTO;
        
        this.init();
    }
    
    /**
     * Initialise le gestionnaire de thèmes
     */
    init() {
        this.applyTheme(this.currentTheme);
        this.setupEventListeners();
        this.updateThemeButtons();
        
        // Écouter les changements de préférence système
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
                if (this.currentTheme === this.themes.AUTO) {
                    this.applyTheme(this.themes.AUTO);
                }
            });
        }
        
        console.log('ThemeManager initialized with theme:', this.currentTheme);
    }
    
    /**
     * Configuration des écouteurs d'événements
     */
    setupEventListeners() {
        // Boutons de changement de thème
        document.addEventListener('click', (event) => {
            const themeBtn = event.target.closest('[data-gc-theme]');
            if (themeBtn) {
                event.preventDefault();
                const theme = themeBtn.dataset.gcTheme;
                this.setTheme(theme);
            }
        });
        
        // Raccourci clavier Ctrl+Shift+T pour changer le thème
        document.addEventListener('keydown', (event) => {
            if (event.ctrlKey && event.shiftKey && event.key === 'T') {
                event.preventDefault();
                this.toggleTheme();
            }
        });
    }
    
    /**
     * Applique un thème donné
     */
    applyTheme(theme) {
        const htmlElement = document.documentElement;
        
        switch (theme) {
            case this.themes.LIGHT:
                htmlElement.setAttribute('data-bs-theme', 'light');
                break;
            case this.themes.DARK:
                htmlElement.setAttribute('data-bs-theme', 'dark');
                break;
            case this.themes.AUTO:
                const prefersDark = window.matchMedia && 
                                  window.matchMedia('(prefers-color-scheme: dark)').matches;
                htmlElement.setAttribute('data-bs-theme', prefersDark ? 'dark' : 'light');
                break;
            default:
                htmlElement.setAttribute('data-bs-theme', 'light');
        }
        
        // Déclencher un événement personnalisé
        const event = new CustomEvent('themeChanged', { 
            detail: { 
                theme: theme,
                actualTheme: htmlElement.getAttribute('data-bs-theme')
            }
        });
        document.dispatchEvent(event);
    }
    
    /**
     * Définit et persiste un nouveau thème
     */
    setTheme(theme) {
        if (!Object.values(this.themes).includes(theme)) {
            console.warn('Invalid theme:', theme);
            return;
        }
        
        this.currentTheme = theme;
        this.storeTheme(theme);
        this.applyTheme(theme);
        this.updateThemeButtons();
        
        console.log('Theme changed to:', theme);
    }
    
    /**
     * Bascule entre les thèmes clair et sombre
     */
    toggleTheme() {
        const currentActualTheme = document.documentElement.getAttribute('data-bs-theme');
        const newTheme = currentActualTheme === 'dark' ? this.themes.LIGHT : this.themes.DARK;
        this.setTheme(newTheme);
    }
    
    /**
     * Met à jour l'état visuel des boutons de thème
     */
    updateThemeButtons() {
        const themeButtons = document.querySelectorAll('[data-gc-theme]');
        
        themeButtons.forEach(btn => {
            const btnTheme = btn.dataset.gcTheme;
            const isActive = btnTheme === this.currentTheme;
            
            btn.classList.toggle('active', isActive);
            btn.setAttribute('aria-pressed', isActive);
            
            // Mettre à jour les icônes si présentes
            const icon = btn.querySelector('i');
            if (icon && isActive) {
                btn.classList.add('text-primary');
            } else {
                btn.classList.remove('text-primary');
            }
        });
        
        // Mettre à jour le texte du thème actuel
        const currentThemeText = document.getElementById('current-theme-text');
        if (currentThemeText) {
            const themeLabels = {
                [this.themes.LIGHT]: 'Clair',
                [this.themes.DARK]: 'Sombre',
                [this.themes.AUTO]: 'Automatique'
            };
            currentThemeText.textContent = themeLabels[this.currentTheme] || 'Inconnu';
        }
    }
    
    /**
     * Récupère le thème stocké
     */
    getStoredTheme() {
        try {
            return localStorage.getItem(this.storageKey);
        } catch (error) {
            console.warn('Could not access localStorage for theme:', error);
            return null;
        }
    }
    
    /**
     * Stocke le thème en localStorage
     */
    storeTheme(theme) {
        try {
            localStorage.setItem(this.storageKey, theme);
        } catch (error) {
            console.warn('Could not save theme to localStorage:', error);
        }
    }
    
    /**
     * Récupère le thème actuel
     */
    getCurrentTheme() {
        return this.currentTheme;
    }
    
    /**
     * Récupère le thème effectivement appliqué (light/dark seulement)
     */
    getActualTheme() {
        return document.documentElement.getAttribute('data-bs-theme');
    }
}

// Initialiser le gestionnaire de thèmes quand le DOM est prêt
document.addEventListener('DOMContentLoaded', () => {
    window.themeManager = new ThemeManager();
});

// Exposer la classe pour utilisation externe
window.ThemeManager = ThemeManager;
