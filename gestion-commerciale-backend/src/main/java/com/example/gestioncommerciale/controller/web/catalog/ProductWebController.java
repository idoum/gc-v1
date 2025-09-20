/*
 * @path src/main/java/com/example/gestioncommerciale/controller/web/catalog/ProductWebController.java
 * @description Contrôleur Web MVC pour l'interface utilisateur des produits avec HTMX
 */
package com.example.gestioncommerciale.controller.web.catalog;

import com.example.gestioncommerciale.dto.catalog.ProductDTO;
import com.example.gestioncommerciale.model.catalog.Product;
import com.example.gestioncommerciale.service.catalog.CategoryService;
import com.example.gestioncommerciale.service.catalog.ProductService;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/catalog/products")
@RequiredArgsConstructor
@Slf4j
public class ProductWebController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    
    /**
     * Liste paginée des produits avec filtres avancés
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Product.ProductStatus status,
            @RequestParam(required = false) Product.ProductType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("Liste produits - search: {}, active: {}, categoryId: {}, status: {}, page: {}", 
                  search, active, categoryId, status, page);
        
        // Créer le Pageable avec tri
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Récupérer les produits avec filtres
        Page<ProductDTO> products = productService.findWithFilters(
            active, categoryId, status, type, minPrice, maxPrice, search, pageable
        );
        
        // Préparer le modèle
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("active", active);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        
        // Données pour les filtres
        model.addAttribute("categories", categoryService.findActiveCategories());
        model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
        model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
        
        // Statistiques
        model.addAttribute("totalProducts", productService.countAll());
        model.addAttribute("activeProducts", productService.countActiveProducts());
        model.addAttribute("lowStockProducts", productService.countLowStockProducts());
        model.addAttribute("outOfStockProducts", productService.countOutOfStockProducts());
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "Catalogue", "url", "/catalog"),
            Map.of("label", "Produits", "url", "/catalog/products")
        ));
        
        // Actions de page
        model.addAttribute("pageActions", List.of(
            Map.of("label", "Nouveau produit", "icon", "bi-plus", "variant", "btn-primary", 
                   "type", "modal", "url", "/catalog/products/new", "target", "#modal-container"),
            Map.of("label", "Import CSV", "icon", "bi-upload", "variant", "btn-outline-secondary", 
                   "type", "modal", "url", "/catalog/products/import", "target", "#modal-container"),
            Map.of("label", "Export CSV", "icon", "bi-download", "variant", "btn-outline-secondary", 
                   "type", "action", "url", "/catalog/products/export")
        ));
        
        // Si requête HTMX, retourner seulement le fragment de tableau
        if (isHtmxRequest(request)) {
            return "catalog/products/list :: product-table";
        }
        
        return "catalog/products/list";
    }
    
    /**
     * Vue détaillée d'un produit
     */
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getById(id);
        model.addAttribute("product", product);
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "Catalogue", "url", "/catalog"),
            Map.of("label", "Produits", "url", "/catalog/products"),
            Map.of("label", product.getName(), "url", "/catalog/products/" + id)
        ));
        
        return "catalog/products/detail";
    }
    
    /**
     * Fragment de ligne produit pour HTMX
     */
    @GetMapping("/_row/{id}")
    public String productRow(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getById(id);
        model.addAttribute("product", product);
        return "catalog/products/_productRow :: product-row";
    }
    
    /**
     * Recherche instantanée HTMX
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Product.ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ProductDTO> products = productService.findWithFilters(
            active, categoryId, status, null, null, null, q, pageable
        );
        
        model.addAttribute("products", products);
        model.addAttribute("search", q);
        model.addAttribute("active", active);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        
        return "catalog/products/list :: product-table";
    }
    
    /**
     * Modal de création d'un nouveau produit
     */
    @GetMapping("/new")
    public String newProductModal(@RequestParam(required = false) Long categoryId, Model model) {
        ProductDTO product = new ProductDTO();
        if (categoryId != null) {
            product.setCategoryId(categoryId);
        }
        
        model.addAttribute("product", product);
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", categoryService.findActiveCategories());
        model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
        model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
        
        return "catalog/products/_productFormModal :: product-form-modal";
    }
    
    /**
     * Modal d'édition d'un produit existant
     */
    @GetMapping("/{id}/edit")
    public String editProductModal(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", categoryService.findActiveCategories());
        model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
        model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
        
        return "catalog/products/_productFormModal :: product-form-modal";
    }
    
    /**
     * Création d'un nouveau produit (POST)
     */
    @PostMapping
    public String createProduct(
            @Valid @ModelAttribute ProductDTO product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la création du produit: {}", bindingResult.getAllErrors());
            model.addAttribute("product", product);
            model.addAttribute("isEdit", false);
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
            model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
            
            if (isHtmxRequest(request)) {
                return "catalog/products/_productFormModal :: product-form-modal";
            }
            return "catalog/products/list";
        }
        
        try {
            ProductDTO created = productService.create(product);
            log.info("Produit créé avec succès: {}", created.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("product", created);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Produit " + created.getName() + " créé avec succès");
                
                request.setAttribute("HX-Trigger", "productCreated");
                return "catalog/products/_productRow :: product-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Produit " + created.getName() + " créé avec succès");
            return "redirect:/catalog/products";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du produit", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                model.addAttribute("product", product);
                model.addAttribute("isEdit", false);
                model.addAttribute("categories", categoryService.findActiveCategories());
                model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
                model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
                return "catalog/products/_productFormModal :: product-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/products";
        }
    }
    
    /**
     * Mise à jour d'un produit existant (PUT/POST)
     */
    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductDTO product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la mise à jour du produit: {}", bindingResult.getAllErrors());
            product.setId(id);
            model.addAttribute("product", product);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
            model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
            
            if (isHtmxRequest(request)) {
                return "catalog/products/_productFormModal :: product-form-modal";
            }
            return "catalog/products/list";
        }
        
        try {
            ProductDTO updated = productService.update(id, product);
            log.info("Produit mis à jour avec succès: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("product", updated);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Produit " + updated.getName() + " mis à jour avec succès");
                
                request.setAttribute("HX-Trigger", "productUpdated");
                return "catalog/products/_productRow :: product-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Produit " + updated.getName() + " mis à jour avec succès");
            return "redirect:/catalog/products";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                product.setId(id);
                model.addAttribute("product", product);
                model.addAttribute("isEdit", true);
                model.addAttribute("categories", categoryService.findActiveCategories());
                model.addAttribute("productStatuses", Arrays.asList(Product.ProductStatus.values()));
                model.addAttribute("productTypes", Arrays.asList(Product.ProductType.values()));
                return "catalog/products/_productFormModal :: product-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/products";
        }
    }
    
    /**
     * Suppression d'un produit (DELETE)
     */
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ProductDTO product = productService.getById(id);
            productService.delete(id);
            log.info("Produit supprimé avec succès: {}", product.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "productDeleted");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Produit " + product.getName() + " supprimé avec succès");
            return "redirect:/catalog/products";
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du produit", e);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "productDeleteError");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/products";
        }
    }
    
    /**
     * Modal de mise à jour du stock
     */
    @GetMapping("/{id}/stock")
    public String stockUpdateModal(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getById(id);
        if (!product.getStockManaged()) {
            throw new IllegalArgumentException("Ce produit n'a pas de gestion de stock activée");
        }
        
        model.addAttribute("product", product);
        return "catalog/products/_stockUpdateModal :: stock-update-modal";
    }
    
    /**
     * Mise à jour du stock (POST)
     */
    @PostMapping("/{id}/stock")
    public String updateStock(
            @PathVariable Long id,
            @RequestParam Integer newQuantity,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ProductDTO updated = productService.updateStock(id, newQuantity);
            log.info("Stock mis à jour pour le produit {}: {} unités", updated.getCode(), newQuantity);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "stockUpdated");
                return "catalog/products/_productRow :: product-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Stock mis à jour: " + newQuantity + " unités");
            return "redirect:/catalog/products";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du stock", e);
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/catalog/products";
        }
    }
    
    /**
     * Vérifie si la requête vient de HTMX
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
