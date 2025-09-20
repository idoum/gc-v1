/*
 * @path src/main/resources/static/js/pages/catalog/category-tree.js
 * @description JavaScript pour la navigation hiérarchique dans l'arbre de catégories
 */

// Configuration de l'arbre de catégories
document.addEventListener('DOMContentLoaded', function() {
    console.log('Arbre de catégories initialisé');
    
    // Initialiser les interactions de l'arbre
    initializeCategoryTree();
    
    // Gérer les événements de mise à jour
    setupTreeEventHandlers();
});

/**
 * Initialise l'arbre de catégories avec Alpine.js
 */
function categoryTree() {
    return {
        expandedNodes: new Set(),
        
        initTree() {
            // Développer tous les nœuds racines par défaut
            this.expandRootNodes();
            
            // Écouter les changements d'état
            this.setupNodeListeners();
        },
        
        expandRootNodes() {
            const rootNodes = document.querySelectorAll('.category-node');
            rootNodes.forEach(node => {
                const categoryId = this.getCategoryId(node);
                if (categoryId) {
                    this.expandedNodes.add(categoryId);
                }
            });
        },
        
        toggleNode(categoryId) {
            if (this.expandedNodes.has(categoryId)) {
                this.expandedNodes.delete(categoryId);
            } else {
                this.expandedNodes.add(categoryId);
            }
        },
        
        isNodeExpanded(categoryId) {
            return this.expandedNodes.has(categoryId);
        },
        
        getCategoryId(node) {
            const categoryItem = node.querySelector('.category-item');
            return categoryItem ? categoryItem.dataset.categoryId : null;
        },
        
        setupNodeListeners() {
            // Écouter les clics sur les boutons d'expansion
            document.addEventListener('click', (event) => {
                const expandButton = event.target.closest('.expand-button');
                if (expandButton) {
                    const categoryId = expandButton.dataset.categoryId;
                    this.toggleNode(categoryId);
                }
            });
        }
    }
}

/**
 * Initialise les interactions de l'arbre
 */
function initializeCategoryTree() {
    // Ajouter les animations CSS pour les transitions
    addTreeAnimations();
    
    // Initialiser le drag & drop pour réorganiser (optionnel)
    if (typeof Sortable !== 'undefined') {
        initializeDragAndDrop();
    }
}

/**
 * Ajoute les animations CSS pour les transitions de l'arbre
 */
function addTreeAnimations() {
    const style = document.createElement('style');
    style.textContent = `
        .category-tree .rotate-minus-90 {
            transform: rotate(-90deg);
            transition: transform 0.2s ease;
        }
        
        .category-item {
            transition: all 0.2s ease;
            border-radius: 8px;
        }
        
        .category-item:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transform: translateY(-1px);
        }
        
        .category-node {
            transition: opacity 0.2s ease;
        }
        
        .category-children {
            overflow: hidden;
            transition: max-height 0.3s ease;
        }
        
        .category-children.collapsed {
            max-height: 0;
        }
        
        .category-children.expanded {
            max-height: 1000px;
        }
    `;
    
    document.head.appendChild(style);
}

/**
 * Configuration des gestionnaires d'événements pour l'arbre
 */
function setupTreeEventHandlers() {
    // Mise à jour de l'arbre après modification d'une catégorie
    document.body.addEventListener('categoryCreated', function(event) {
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    });
    
    document.body.addEventListener('categoryUpdated', function(event) {
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    });
    
    document.body.addEventListener('categoryDeleted', function(event) {
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    });
}

/**
 * Développe tous les nœuds de l'arbre
 */
function expandAllCategories() {
    const expandButtons = document.querySelectorAll('[data-bs-target]');
    expandButtons.forEach(button => {
        const target = button.getAttribute('data-bs-target');
        const collapse = document.querySelector(target);
        if (collapse) {
            const bsCollapse = new bootstrap.Collapse(collapse, { show: true });
        }
    });
    
    // Rotation des icônes
    const chevrons = document.querySelectorAll('.bi-chevron-down');
    chevrons.forEach(chevron => {
        chevron.classList.remove('rotate-minus-90');
    });
    
    console.log('Toutes les catégories développées');
}

/**
 * Réduit tous les nœuds de l'arbre
 */
function collapseAllCategories() {
    const expandButtons = document.querySelectorAll('[data-bs-target]');
    expandButtons.forEach(button => {
        const target = button.getAttribute('data-bs-target');
        const collapse = document.querySelector(target);
        if (collapse) {
            const bsCollapse = new bootstrap.Collapse(collapse, { hide: true });
        }
    });
    
    // Rotation des icônes
    const chevrons = document.querySelectorAll('.bi-chevron-down');
    chevrons.forEach(chevron => {
        chevron.classList.add('rotate-minus-90');
    });
    
    console.log('Toutes les catégories réduites');
}

/**
 * Initialise le drag & drop pour réorganiser les catégories (optionnel)
 */
function initializeDragAndDrop() {
    const categoryLists = document.querySelectorAll('.category-list');
    
    categoryLists.forEach(list => {
        new Sortable(list, {
            group: 'categories',
            handle: '.category-item',
            animation: 150,
            fallbackOnBody: true,
            swapThreshold: 0.65,
            
            onEnd: function(event) {
                // Appeler l'API pour sauvegarder le nouvel ordre
                const categoryId = event.item.dataset.categoryId;
                const newIndex = event.newIndex;
                const parentId = event.to.dataset.parentId;
                
                updateCategoryOrder(categoryId, newIndex, parentId);
            }
        });
    });
}

/**
 * Met à jour l'ordre d'une catégorie via l'API
 */
function updateCategoryOrder(categoryId, newIndex, parentId) {
    const data = {
        categoryId: categoryId,
        newIndex: newIndex,
        parentId: parentId
    };
    
    htmx.ajax('POST', '/catalog/categories/reorder', {
        values: data,
        swap: 'none'
    }).then(() => {
        console.log('Ordre de catégorie mis à jour:', categoryId);
        showToast(createSuccessToast('Ordre mis à jour'));
    }).catch(error => {
        console.error('Erreur lors de la mise à jour de l\'ordre:', error);
        showToast(createErrorToast('Erreur lors de la mise à jour'));
        // Recharger la page en cas d'erreur
        setTimeout(() => window.location.reload(), 2000);
    });
}

/**
 * Recherche dans l'arbre de catégories
 */
function searchInTree(query) {
    const categoryItems = document.querySelectorAll('.category-item');
    const lowerQuery = query.toLowerCase();
    
    categoryItems.forEach(item => {
        const categoryName = item.querySelector('h6').textContent.toLowerCase();
        const categoryCode = item.querySelector('code').textContent.toLowerCase();
        const categoryDesc = item.querySelector('.text-muted')?.textContent?.toLowerCase() || '';
        
        const matches = categoryName.includes(lowerQuery) || 
                       categoryCode.includes(lowerQuery) || 
                       categoryDesc.includes(lowerQuery);
        
        if (matches || !query) {
            item.style.display = '';
            item.parentNode.style.display = '';
            
            // Développer le parent si trouvé
            if (matches && query) {
                const parentCollapse = item.closest('.collapse');
                if (parentCollapse) {
                    const bsCollapse = new bootstrap.Collapse(parentCollapse, { show: true });
                }
            }
        } else {
            item.style.display = 'none';
        }
    });
    
    // Masquer les catégories parents qui n'ont plus d'enfants visibles
    const categoryNodes = document.querySelectorAll('.category-node');
    categoryNodes.forEach(node => {
        const visibleChildren = node.querySelectorAll('.category-item:not([style*="display: none"])').length;
        if (visibleChildren === 0 && query) {
            node.style.display = 'none';
        } else {
            node.style.display = '';
        }
    });
}

// Exposer les fonctions globalement
window.expandAllCategories = expandAllCategories;
window.collapseAllCategories = collapseAllCategories;
window.categoryTree = categoryTree;
window.searchInTree = searchInTree;
