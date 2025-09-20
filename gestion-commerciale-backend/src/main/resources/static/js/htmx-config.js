/*
 * @path src/main/resources/static/js/htmx-config.js
 * @description Configuration globale HTMX avec gestion des erreurs et événements
 */

// Configuration HTMX
htmx.config.globalViewTransitions = true;
htmx.config.defaultSwapStyle = 'innerHTML';
htmx.config.defaultSwapDelay = 0;
htmx.config.defaultSettleDelay = 20;

// Gestionnaire d'erreurs HTMX
document.body.addEventListener('htmx:responseError', function(event) {
    console.error('HTMX Error:', event.detail);
    
    const toast = createErrorToast('Une erreur s\'est produite. Veuillez réessayer.');
    showToast(toast);
});

// Gestionnaire de succès HTMX
document.body.addEventListener('htmx:afterSwap', function(event) {
    // Réinitialiser les tooltips Bootstrap après swap
    initializeTooltips();
    
    // Déclencher Alpine.js sur les nouveaux éléments
    if (typeof Alpine !== 'undefined') {
        Alpine.initTree(event.target);
    }
});

// Loading states
document.body.addEventListener('htmx:beforeRequest', function(event) {
    const trigger = event.detail.elt;
    if (trigger.classList.contains('btn')) {
        trigger.classList.add('loading');
        const spinner = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>';
        trigger.insertAdjacentHTML('afterbegin', spinner);
    }
});

document.body.addEventListener('htmx:afterRequest', function(event) {
    const trigger = event.detail.elt;
    if (trigger.classList.contains('btn')) {
        trigger.classList.remove('loading');
        const spinner = trigger.querySelector('.spinner-border');
        if (spinner) spinner.remove();
    }
});

// Fonctions utilitaires
function createSuccessToast(message) {
    return createToast(message, 'success', 'bi-check-circle');
}

function createErrorToast(message) {
    return createToast(message, 'danger', 'bi-exclamation-triangle');
}

function createInfoToast(message) {
    return createToast(message, 'info', 'bi-info-circle');
}

function createToast(message, type = 'info', icon = 'bi-info-circle') {
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div class="toast align-items-center text-bg-${type} border-0" role="alert" id="${toastId}">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi ${icon} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                        data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    const container = document.getElementById('toast-container');
    container.insertAdjacentHTML('beforeend', toastHtml);
    
    return document.getElementById(toastId);
}

function showToast(toastElement) {
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 5000
    });
    toast.show();
    
    // Nettoyer après disparition
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}

function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Initialiser les tooltips au chargement
document.addEventListener('DOMContentLoaded', initializeTooltips);

// Gestionnaire de confirmation pour suppressions
document.body.addEventListener('htmx:confirm', function(event) {
    const confirmMessage = event.target.getAttribute('hx-confirm') || 'Êtes-vous sûr ?';
    event.preventDefault();
    
    if (confirm(confirmMessage)) {
        event.detail.issueRequest();
    }
});
