/*
 * @path src/main/java/com/example/gestioncommerciale/controller/web/catalog/CategoryWebController.java
 * @description Contrôleur Web MVC pour l'interface utilisateur des catégories avec HTMX
 */
package com.example.gestioncommerciale.controller.web.catalog;

import com.example.gestioncommerciale.dto.catalog.CategoryDTO;
import com.example.gestioncommerciale.service.catalog.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/catalog/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryWebController {
    
    private final CategoryService categoryService;
    
    /**
     * Liste paginée des catégories avec filtres et recherche
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("Liste catégories - search: {}, active: {}, parentId: {}, page: {}", search, active, parentId, page);
        
        // Créer le Pageable avec tri
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Récupérer les catégories avec filtres
        Page<CategoryDTO> categories = categoryService.findWithFilters(active, parentId, search, pageable);
        
        // Préparer le modèle
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("active", active);
        model.addAttribute("parentId", parentId);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        
        // Arbre de catégories pour le sélecteur parent
        model.addAttribute("categoryTree", categoryService.findCategoryTree());
        
        // Statistiques
        model.addAttribute("totalCategories", categoryService.countAll());
        model.addAttribute("activeCategories", categoryService.countActiveCategories());
        model.addAttribute("rootCategories", categoryService.countRootCategories());
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "Catalogue", "url", "/catalog"),
            Map.of("label", "Catégories", "url", "/catalog/categories")
        ));
        
        // Actions de page
        model.addAttribute("pageActions", List.of(
            Map.of("label", "Nouvelle catégorie", "icon", "bi-plus", "variant", "btn-primary", 
                   "type", "modal", "url", "/catalog/categories/new", "target", "#modal-container"),
            Map.of("label", "Vue arbre", "icon", "bi-diagram-3", "variant", "btn-outline-secondary", 
                   "type", "link", "url", "/catalog/categories/tree")
        ));
        
        // Si requête HTMX, retourner seulement le fragment de tableau
        if (isHtmxRequest(request)) {
            return "catalog/categories/list :: category-table";
        }
        
        return "catalog/categories/list";
    }
    
    /**
     * Vue arbre hiérarchique des catégories
     */
    @GetMapping("/tree")
    public String categoryTree(Model model) {
        List<CategoryDTO> categoryTree = categoryService.findCategoryTree();
        model.addAttribute("categoryTree", categoryTree);
        
        // Statistiques
        model.addAttribute("totalCategories", categoryService.countAll());
        model.addAttribute("activeCategories", categoryService.countActiveCategories());
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "Catalogue", "url", "/catalog"),
            Map.of("label", "Catégories", "url", "/catalog/categories"),
            Map.of("label", "Vue arbre", "url", "/catalog/categories/tree")
        ));
        
        // Actions de page
        model.addAttribute("pageActions", List.of(
            Map.of("label", "Nouvelle catégorie", "icon", "bi-plus", "variant", "btn-primary", 
                   "type", "modal", "url", "/catalog/categories/new", "target", "#modal-container"),
            Map.of("label", "Vue liste", "icon", "bi-list", "variant", "btn-outline-secondary", 
                   "type", "link", "url", "/catalog/categories")
        ));
        
        return "catalog/categories/tree";
    }
    
    /**
     * Fragment de ligne catégorie pour HTMX
     */
    @GetMapping("/_row/{id}")
    public String categoryRow(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getById(id);
        model.addAttribute("category", category);
        return "catalog/categories/_categoryRow :: category-row";
    }
    
    /**
     * Recherche instantanée HTMX
     */
    @GetMapping("/search")
    public String searchCategories(
            @RequestParam String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder", "name"));
        Page<CategoryDTO> categories = categoryService.findWithFilters(active, parentId, q, pageable);
        
        model.addAttribute("categories", categories);
        model.addAttribute("search", q);
        model.addAttribute("active", active);
        model.addAttribute("parentId", parentId);
        
        return "catalog/categories/list :: category-table";
    }
    
    /**
     * Modal de création d'une nouvelle catégorie
     */
    @GetMapping("/new")
    public String newCategoryModal(@RequestParam(required = false) Long parentId, Model model) {
        CategoryDTO category = new CategoryDTO();
        if (parentId != null) {
            category.setParentId(parentId);
            CategoryDTO parent = categoryService.getById(parentId);
            model.addAttribute("parentCategory", parent);
        }
        
        model.addAttribute("category", category);
        model.addAttribute("isEdit", false);
        model.addAttribute("categoryTree", categoryService.findCategoryTree());
        
        return "catalog/categories/_categoryFormModal :: category-form-modal";
    }
    
    /**
     * Modal d'édition d'une catégorie existante
     */
    @GetMapping("/{id}/edit")
    public String editCategoryModal(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getById(id);
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        model.addAttribute("categoryTree", categoryService.findCategoryTree());
        
        return "catalog/categories/_categoryFormModal :: category-form-modal";
    }
    
    /**
     * Création d'une nouvelle catégorie (POST)
     */
    @PostMapping
    public String createCategory(
            @Valid @ModelAttribute CategoryDTO category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la création de la catégorie: {}", bindingResult.getAllErrors());
            model.addAttribute("category", category);
            model.addAttribute("isEdit", false);
            model.addAttribute("categoryTree", categoryService.findCategoryTree());
            
            if (isHtmxRequest(request)) {
                return "catalog/categories/_categoryFormModal :: category-form-modal";
            }
            return "catalog/categories/list";
        }
        
        try {
            CategoryDTO created = categoryService.create(category);
            log.info("Catégorie créée avec succès: {}", created.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("category", created);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Catégorie " + created.getName() + " créée avec succès");
                
                request.setAttribute("HX-Trigger", "categoryCreated");
                return "catalog/categories/_categoryRow :: category-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Catégorie " + created.getName() + " créée avec succès");
            return "redirect:/catalog/categories";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de la catégorie", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                model.addAttribute("category", category);
                model.addAttribute("isEdit", false);
                model.addAttribute("categoryTree", categoryService.findCategoryTree());
                return "catalog/categories/_categoryFormModal :: category-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/categories";
        }
    }
    
    /**
     * Mise à jour d'une catégorie existante (PUT/POST)
     */
    @PostMapping("/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute CategoryDTO category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la mise à jour de la catégorie: {}", bindingResult.getAllErrors());
            category.setId(id);
            model.addAttribute("category", category);
            model.addAttribute("isEdit", true);
            model.addAttribute("categoryTree", categoryService.findCategoryTree());
            
            if (isHtmxRequest(request)) {
                return "catalog/categories/_categoryFormModal :: category-form-modal";
            }
            return "catalog/categories/list";
        }
        
        try {
            CategoryDTO updated = categoryService.update(id, category);
            log.info("Catégorie mise à jour avec succès: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("category", updated);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Catégorie " + updated.getName() + " mise à jour avec succès");
                
                request.setAttribute("HX-Trigger", "categoryUpdated");
                return "catalog/categories/_categoryRow :: category-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Catégorie " + updated.getName() + " mise à jour avec succès");
            return "redirect:/catalog/categories";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la catégorie", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                category.setId(id);
                model.addAttribute("category", category);
                model.addAttribute("isEdit", true);
                model.addAttribute("categoryTree", categoryService.findCategoryTree());
                return "catalog/categories/_categoryFormModal :: category-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/categories";
        }
    }
    
    /**
     * Suppression d'une catégorie (DELETE)
     */
    @DeleteMapping("/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            CategoryDTO category = categoryService.getById(id);
            categoryService.delete(id);
            log.info("Catégorie supprimée avec succès: {}", category.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "categoryDeleted");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Catégorie " + category.getName() + " supprimée avec succès");
            return "redirect:/catalog/categories";
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la catégorie", e);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "categoryDeleteError");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/categories";
        }
    }
    
    /**
     * API pour récupérer les enfants d'une catégorie (pour l'arbre)
     */
    @GetMapping("/{id}/children")
    @ResponseBody
    public List<CategoryDTO> getCategoryChildren(@PathVariable Long id) {
        return categoryService.findByParent(id);
    }
    
    /**
     * Vérifie si la requête vient de HTMX
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}

