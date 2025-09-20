/*
 * @path src/main/resources/static/js/alpine-init.js
 * @description Configuration et stores globaux Alpine.js
 */

// Stores globaux Alpine.js
document.addEventListener('alpine:init', () => {
    // Store pour gestion des modals
    Alpine.store('modal', {
        current: null,
        
        open(modalId) {
            this.current = modalId;
            const modalEl = document.getElementById(modalId);
            if (modalEl) {
                const modal = new bootstrap.Modal(modalEl);
                modal.show();
            }
        },
        
        close() {
            if (this.current) {
                const modalEl = document.getElementById(this.current);
                if (modalEl) {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }
                this.current = null;
            }
        }
    });
    
    // Store pour gestion des filtres
    Alpine.store('filters', {
        search: '',
        status: '',
        type: '',
        page: 1,
        size: 20,
        sort: 'companyName',
        direction: 'asc',
        
        reset() {
            this.search = '';
            this.status = '';
            this.type = '';
            this.page = 1;
        },
        
        toQueryString() {
            const params = new URLSearchParams();
            if (this.search) params.append('search', this.search);
            if (this.status) params.append('status', this.status);
            if (this.type) params.append('type', this.type);
            params.append('page', this.page);
            params.append('size', this.size);
            params.append('sort', this.sort);
            params.append('direction', this.direction);
            return params.toString();
        }
    });
    
    // Store pour notifications
    Alpine.store('notifications', {
        items: [],
        
        add(message, type = 'info') {
            const id = Date.now();
            this.items.push({ id, message, type });
            
            // Auto-remove après 5 secondes
            setTimeout(() => this.remove(id), 5000);
        },
        
        remove(id) {
            this.items = this.items.filter(item => item.id !== id);
        },
        
        success(message) { this.add(message, 'success'); },
        error(message) { this.add(message, 'error'); },
        info(message) { this.add(message, 'info'); },
        warning(message) { this.add(message, 'warning'); }
    });
});

// Composants Alpine.js globaux
Alpine.data('searchable', () => ({
    query: '',
    results: [],
    loading: false,
    debounceTimeout: null,
    
    search() {
        clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => {
            this.performSearch();
        }, 300);
    },
    
    async performSearch() {
        if (this.query.length < 2) {
            this.results = [];
            return;
        }
        
        this.loading = true;
        
        try {
            const response = await fetch(`/api/search?q=${encodeURIComponent(this.query)}`);
            this.results = await response.json();
        } catch (error) {
            console.error('Search error:', error);
            this.results = [];
        } finally {
            this.loading = false;
        }
    }
}));

Alpine.data('formValidator', (rules = {}) => ({
    errors: {},
    touched: {},
    
    validate(field, value) {
        const fieldRules = rules[field] || [];
        const errors = [];
        
        for (const rule of fieldRules) {
            if (typeof rule === 'function') {
                const result = rule(value);
                if (result !== true) {
                    errors.push(result);
                }
            }
        }
        
        this.errors[field] = errors;
        return errors.length === 0;
    },
    
    touch(field) {
        this.touched[field] = true;
    },
    
    isValid() {
        return Object.keys(this.errors).every(field => 
            !this.errors[field] || this.errors[field].length === 0
        );
    },
    
    getError(field) {
        return this.touched[field] && this.errors[field] && this.errors[field][0];
    }
}));

// Utilitaires globaux
window.Alpine = Alpine;

// Directives personnalisées Alpine.js
Alpine.directive('tooltip', (el, { expression }, { evaluate }) => {
    const tooltip = new bootstrap.Tooltip(el, {
        title: evaluate(expression),
        placement: 'top'
    });
    
    // Cleanup
    el._x_tooltip = tooltip;
    Alpine.mutateDom(() => {
        if (el._x_tooltip) {
            el._x_tooltip.dispose();
        }
    });
});

// Magic properties
Alpine.magic('toast', () => ({
    success: (message) => showToast(createSuccessToast(message)),
    error: (message) => showToast(createErrorToast(message)),
    info: (message) => showToast(createInfoToast(message))
}));
