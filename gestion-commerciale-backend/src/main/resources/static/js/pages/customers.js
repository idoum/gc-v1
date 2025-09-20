/*
 * @path src/main/resources/static/js/pages/customers.js
 * @description JavaScript spécialisé pour la gestion des clients avec HTMX
 */

// Configuration spécifique aux clients
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page clients initialisée');
    
    // Gestionnaire d'événements HTMX pour les clients
    setupCustomerEventHandlers();
    
    // Initialiser les tooltips
    initializeCustomerTooltips();
    
    // Configurer les raccourcis clavier
    setupCustomerKeyboardShortcuts();
});

/**
 * Configuration des gestionnaires d'événements spécifiques aux clients
 */
function setupCustomerEventHandlers() {
    // Succès création client
    document.body.addEventListener('customerCreated', function(event) {
        console.log('Client créé avec succès');
        showToast(createSuccessToast('Client créé avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/customers', {
            target: '#customer-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Succès mise à jour client
    document.body.addEventListener('customerUpdated', function(event) {
        console.log('Client mis à jour avec succès');
        showToast(createSuccessToast('Client mis à jour avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/customers', {
            target: '#customer-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Succès suppression client
    document.body.addEventListener('customerDeleted', function(event) {
        console.log('Client supprimé avec succès');
        showToast(createSuccessToast('Client supprimé avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/customers', {
            target: '#customer-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Erreur suppression client
    document.body.addEventListener('customerDeleteError', function(event) {
        console.error('Erreur lors de la suppression du client');
        showToast(createErrorToast('Erreur lors de la suppression du client'));
    });
    
    // Gestion des erreurs de validation
    document.body.addEventListener('htmx:responseError', function(event) {
        if (event.detail.xhr.status === 422) {
            console.warn('Erreurs de validation côté serveur');
            // Les erreurs sont déjà affichées dans le formulaire
        }
    });
}

/**
 * Initialisation des tooltips pour les éléments clients
 */
function initializeCustomerTooltips() {
    const tooltipElements = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipElements.forEach(function(element) {
        new bootstrap.Tooltip(element);
    });
}

/**
 * Configuration des raccourcis clavier pour la page clients
 */
function setupCustomerKeyboardShortcuts() {
    document.addEventListener('keydown', function(event) {
        // Ctrl + N = Nouveau client
        if (event.ctrlKey && event.key === 'n') {
            event.preventDefault();
            const newButton = document.querySelector('[hx-get="/customers/new"]');
            if (newButton) {
                newButton.click();
            }
        }
        
        // Echap = Fermer modal
        if (event.key === 'Escape') {
            const modal = bootstrap.Modal.getInstance(document.getElementById('customerFormModal'));
            if (modal) {
                modal.hide();
            }
        }
        
        // F3 = Focus sur recherche
        if (event.key === 'F3') {
            event.preventDefault();
            const searchInput = document.querySelector('input[x-model="search"]');
            if (searchInput) {
                searchInput.focus();
            }
        }
    });
}

/**
 * Fonction pour valider le formulaire client côté client
 */
function validateCustomerForm(formElement) {
    let isValid = true;
    const errors = [];
    
    // Validation nom de société
    const companyName = formElement.querySelector('#companyName');
    if (!companyName.value.trim()) {
        errors.push('Le nom de société est requis');
        markFieldAsInvalid(companyName);
        isValid = false;
    } else {
        markFieldAsValid(companyName);
    }
    
    // Validation email si renseigné
    const email = formElement.querySelector('#email');
    if (email.value.trim() && !isValidEmail(email.value)) {
        errors.push('Format d\'email invalide');
        markFieldAsInvalid(email);
        isValid = false;
    } else if (email.value.trim()) {
        markFieldAsValid(email);
    }
    
    // Validation SIRET si renseigné
    const siret = formElement.querySelector('#siret');
    if (siret.value.trim() && !isValidSiret(siret.value)) {
        errors.push('Format SIRET invalide (14 chiffres requis)');
        markFieldAsInvalid(siret);
        isValid = false;
    } else if (siret.value.trim()) {
        markFieldAsValid(siret);
    }
    
    return { isValid, errors };
}

/**
 * Marque un champ comme invalide
 */
function markFieldAsInvalid(field) {
    field.classList.remove('is-valid');
    field.classList.add('is-invalid');
}

/**
 * Marque un champ comme valide
 */
function markFieldAsValid(field) {
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');
}

/**
 * Validation email
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Validation SIRET (14 chiffres)
 */
function isValidSiret(siret) {
    return /^\d{14}$/.test(siret.replace(/\s/g, ''));
}

/**
 * Fonction pour exporter la liste des clients
 */
function exportCustomers(format = 'csv') {
    const params = new URLSearchParams();
    
    // Récupérer les filtres actuels
    const searchInput = document.querySelector('input[x-model="search"]');
    if (searchInput && searchInput.value) {
        params.append('search', searchInput.value);
    }
    
    const statusSelect = document.querySelector('select[x-model="status"]');
    if (statusSelect && statusSelect.value) {
        params.append('status', statusSelect.value);
    }
    
    const typeSelect = document.querySelector('select[x-model="type"]');
    if (typeSelect && typeSelect.value) {
        params.append('type', typeSelect.value);
    }
    
    params.append('format', format);
    
    // Télécharger le fichier
    window.location.href = '/customers/export?' + params.toString();
}

// Exposer les fonctions utilitaires
window.customerUtils = {
    validateForm: validateCustomerForm,
    exportCustomers: exportCustomers,
    isValidEmail: isValidEmail,
    isValidSiret: isValidSiret
};
