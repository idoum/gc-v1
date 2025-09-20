/*
 * @path src/main/resources/static/js/pages/catalog/categories.js
 * @description JavaScript spécialisé pour la gestion des catégories avec HTMX
 */

// Configuration spécifique aux catégories
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page catégories initialisée');
    
    // Gestionnaire d'événements HTMX pour les catégories
    setupCategoryEventHandlers();
    
    // Initialiser les tooltips
    initializeCategoryTooltips();
    
    // Configurer les raccourcis clavier
    setupCategoryKeyboardShortcuts();
});

/**
 * Configuration des gestionnaires d'événements spécifiques aux catégories
 */
function setupCategoryEventHandlers() {
    // Succès création catégorie
    document.body.addEventListener('categoryCreated', function(event) {
        console.log('Catégorie créée avec succès');
        showToast(createSuccessToast('Catégorie créée avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/catalog/categories', {
            target: '#category-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Succès mise à jour catégorie
    document.body.addEventListener('categoryUpdated', function(event) {
        console.log('Catégorie mise à jour avec succès');
        showToast(createSuccessToast('Catégorie mise à jour avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/catalog/categories', {
            target: '#category-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Succès suppression catégorie
    document.body.addEventListener('categoryDeleted', function(event) {
        console.log('Catégorie supprimée avec succès');
        showToast(createSuccessToast('Catégorie supprimée avec succès'));
        
        // Recharger la table
        htmx.ajax('GET', '/catalog/categories', {
            target: '#category-table-container',
            swap: 'innerHTML'
        });
    });
    
    // Erreur suppression catégorie
    document.body.addEventListener('categoryDeleteError', function(event) {
        console.error('Erreur lors de la suppression de la catégorie');
        showToast(createErrorToast('Impossible de supprimer cette catégorie'));
    });
}

/**
 * Initialisation des tooltips pour les éléments catégories
 */
function initializeCategoryTooltips() {
    const tooltipElements = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipElements.forEach(function(element) {
        new bootstrap.Tooltip(element);
    });
}

/**
 * Configuration des raccourcis clavier pour la page catégories
 */
function setupCategoryKeyboardShortcuts() {
    document.addEventListener('keydown', function(event) {
        // Ctrl + N = Nouvelle catégorie
        if (event.ctrlKey && event.key === 'n') {
            event.preventDefault();
            const newButton = document.querySelector('[hx-get="/catalog/categories/new"]');
            if (newButton) {
                newButton.click();
            }
        }
        
        // Echap = Fermer modal
        if (event.key === 'Escape') {
            const modal = bootstrap.Modal.getInstance(document.getElementById('categoryFormModal'));
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
        
        // Ctrl + T = Vue arbre
        if (event.ctrlKey && event.key === 't') {
            event.preventDefault();
            window.location.href = '/catalog/categories/tree';
        }
    });
}

/**
 * Fonction pour valider le formulaire catégorie côté client
 */
function validateCategoryForm(formElement) {
    let isValid = true;
    const errors = [];
    
    // Validation nom
    const name = formElement.querySelector('#categoryName');
    if (!name.value.trim()) {
        errors.push('Le nom de la catégorie est requis');
        markFieldAsInvalid(name);
        isValid = false;
    } else {
        markFieldAsValid(name);
    }
    
    // Validation parent (pas soi-même)
    const parentSelect = formElement.querySelector('#categoryParent');
    const categoryIdInput = formElement.querySelector('input[name="id"]');
    
    if (parentSelect.value && categoryIdInput && parentSelect.value === categoryIdInput.value) {
        errors.push('Une catégorie ne peut pas être son propre parent');
        markFieldAsInvalid(parentSelect);
        isValid = false;
    } else if (parentSelect.value) {
        markFieldAsValid(parentSelect);
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
 * Fonction pour exporter la liste des catégories
 */
function exportCategories(format = 'csv') {
    const params = new URLSearchParams();
    
    // Récupérer les filtres actuels
    const searchInput = document.querySelector('input[x-model="search"]');
    if (searchInput && searchInput.value) {
        params.append('search', searchInput.value);
    }
    
    const activeSelect = document.querySelector('select[x-model="active"]');
    if (activeSelect && activeSelect.value) {
        params.append('active', activeSelect.value);
    }
    
    const parentSelect = document.querySelector('select[x-model="parentId"]');
    if (parentSelect && parentSelect.value) {
        params.append('parentId', parentSelect.value);
    }
    
    params.append('format', format);
    
    // Télécharger le fichier
    window.location.href = '/catalog/categories/export?' + params.toString();
}

// Exposer les fonctions utilitaires
window.categoryUtils = {
    validateForm: validateCategoryForm,
    exportCategories: exportCategories
};
