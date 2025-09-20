/*
 * @path src/main/resources/static/js/pages/crm/contacts.js
 * @description JS pour interactions HTMX et feedbacks sur la gestion des contacts CRM
 */
document.addEventListener('DOMContentLoaded', function() {
    // Feedbacks HTMX
    document.body.addEventListener('contactCreated', function() {
        showToast(createSuccessToast('Contact créé avec succès'));
    });
    document.body.addEventListener('contactUpdated', function() {
        showToast(createSuccessToast('Contact mis à jour'));
    });
    document.body.addEventListener('contactDeleted', function() {
        showToast(createSuccessToast('Contact supprimé'));
    });
    document.body.addEventListener('contactDeleteError', function() {
        showToast(createErrorToast('Erreur lors de la suppression du contact'));
    });
    // Tooltips Bootstrap
    const tooltipElements = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipElements.forEach(function(element) {
        new bootstrap.Tooltip(element);
    });
});
