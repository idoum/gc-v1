/*
 * @path src/main/resources/static/js/catalog-init.js
 * @description Initialisation globale du module catalogue avec styles dynamiques
 */

// Initialisation du module catalogue
document.addEventListener('DOMContentLoaded', function() {
    console.log('Module Catalogue initialisé');
    
    // Initialiser les styles dynamiques
    initializeDynamicStyles();
    
    // Configurer les animations
    setupAnimations();
    
    // Initialiser les indicateurs de stock
    initializeStockIndicators();
    
    // Configurer les tooltips catalogue
    initializeCatalogTooltips();
});

/**
 * Initialise les styles dynamiques basés sur les données
 */
function initializeDynamicStyles() {
    // Jauges de stock dynamiques
    const stockGauges = document.querySelectorAll('.gc-stock-gauge');
    stockGauges.forEach(gauge => {
        const value = parseFloat(gauge.dataset.value || 0);
        const max = parseFloat(gauge.dataset.max || 100);
        const percentage = Math.min((value / max) * 100, 100);
        
        gauge.style.setProperty('--stock-angle', (percentage * 3.6) + 'deg');
        
        // Classes selon le niveau
        gauge.classList.remove('gc-stock-level-high', 'gc-stock-level-medium', 'gc-stock-level-low');
        if (percentage > 60) {
            gauge.classList.add('gc-stock-level-high');
        } else if (percentage > 30) {
            gauge.classList.add('gc-stock-level-medium');
        } else {
            gauge.classList.add('gc-stock-level-low');
        }
    });
    
    // Barres de progression stock
    const stockBars = document.querySelectorAll('.gc-stock-progress');
    stockBars.forEach(bar => {
        const value = parseFloat(bar.dataset.value || 0);
        const max = parseFloat(bar.dataset.max || 100);
        const min = parseFloat(bar.dataset.min || 10);
        const percentage = Math.min((value / max) * 100, 100);
        
        const progressBar = bar.querySelector('.gc-stock-progress-bar');
        if (progressBar) {
            progressBar.style.width = percentage + '%';
            
            // Classes selon le niveau
            bar.classList.remove('gc-stock-progress-ok', 'gc-stock-progress-low', 'gc-stock-progress-out');
            if (value <= 0) {
                bar.classList.add('gc-stock-progress-out');
            } else if (value <= min) {
                bar.classList.add('gc-stock-progress-low');
            } else {
                bar.classList.add('gc-stock-progress-ok');
            }
        }
    });
}

/**
 * Configure les animations et transitions
 */
function setupAnimations() {
    // Observer pour les animations au scroll
    if ('IntersectionObserver' in window) {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('gc-fade-in');
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);
        
        // Observer les éléments à animer
        document.querySelectorAll('.gc-product-card, .gc-category-card, .gc-stats-card').forEach(el => {
            observer.observe(el);
        });
    }
    
    // Animations de hover pour les cartes
    const hoverElements = document.querySelectorAll('.gc-hover-lift, .gc-hover-scale');
    hoverElements.forEach(el => {
        el.addEventListener('mouseenter', function() {
            this.style.transform = this.classList.contains('gc-hover-lift') 
                ? 'translateY(-4px)' 
                : 'scale(1.02)';
        });
        
        el.addEventListener('mouseleave', function() {
            this.style.transform = '';
        });
    });
}

/**
 * Initialise les indicateurs de stock interactifs
 */
function initializeStockIndicators() {
    const stockElements = document.querySelectorAll('[data-stock-level]');
    
    stockElements.forEach(element => {
        const level = element.dataset.stockLevel;
        const value = parseFloat(element.dataset.stockValue || 0);
        
        // Ajouter les classes appropriées
        switch (level) {
            case 'out':
                element.classList.add('gc-pulse-danger');
                break;
            case 'low':
                element.classList.add('text-warning');
                break;
            case 'ok':
                element.classList.add('text-success');
                break;
        }
        
        // Tooltip avec informations détaillées
        if (element.hasAttribute('data-bs-toggle')) {
            const tooltip = new bootstrap.Tooltip(element, {
                html: true,
                title: generateStockTooltip(element)
            });
        }
    });
}

/**
 * Génère le contenu du tooltip pour le stock
 */
function generateStockTooltip(element) {
    const value = element.dataset.stockValue || 0;
    const min = element.dataset.stockMin || 0;
    const max = element.dataset.stockMax || 0;
    const unit = element.dataset.stockUnit || 'pce';
    
    return `
        <div class="text-start">
            <div><strong>Stock actuel:</strong> ${value} ${unit}</div>
            <div><strong>Stock minimum:</strong> ${min} ${unit}</div>
            ${max > 0 ? `<div><strong>Stock maximum:</strong> ${max} ${unit}</div>` : ''}
        </div>
    `;
}

/**
 * Initialise les tooltips spécifiques au catalogue
 */
function initializeCatalogTooltips() {
    // Tooltips pour les badges de statut
    const statusBadges = document.querySelectorAll('.gc-status-badge[data-status]');
    statusBadges.forEach(badge => {
        const status = badge.dataset.status;
        const tooltips = {
            'available': 'Produit disponible à la vente',
            'out_of_stock': 'Produit temporairement indisponible',
            'discontinued': 'Produit retiré du catalogue',
            'pending': 'Produit en attente de validation',
            'draft': 'Produit en cours de création'
        };
        
        new bootstrap.Tooltip(badge, {
            title: tooltips[status] || 'Statut inconnu'
        });
    });
    
    // Tooltips pour les codes produits
    const productCodes = document.querySelectorAll('code[data-product-id]');
    productCodes.forEach(code => {
        new bootstrap.Tooltip(code, {
            title: 'Cliquez pour copier le code'
        });
        
        code.addEventListener('click', function() {
            navigator.clipboard.writeText(this.textContent).then(() => {
                this.classList.add('gc-pulse-success');
                setTimeout(() => {
                    this.classList.remove('gc-pulse-success');
                }, 2000);
            });
        });
    });
}

/**
 * Gestion des thèmes pour le catalogue
 */
function updateCatalogTheme(theme) {
    const root = document.documentElement;
    
    if (theme === 'dark') {
        root.style.setProperty('--gc-light', '#1a1a1a');
        root.style.setProperty('--gc-dark', '#f8f9fa');
    } else {
        root.style.setProperty('--gc-light', '#f8f9fa');
        root.style.setProperty('--gc-dark', '#212529');
    }
    
    // Réinitialiser les styles dynamiques
    setTimeout(initializeDynamicStyles, 100);
}

// Écouter les changements de thème
document.addEventListener('themeChanged', function(event) {
    updateCatalogTheme(event.detail.theme);
});

// Exposer les fonctions utilitaires
window.catalogInit = {
    initializeDynamicStyles,
    updateCatalogTheme,
    initializeStockIndicators
};

// Réinitialiser après les mises à jour HTMX
document.body.addEventListener('htmx:afterSettle', function() {
    initializeDynamicStyles();
    initializeStockIndicators();
    initializeCatalogTooltips();
});
