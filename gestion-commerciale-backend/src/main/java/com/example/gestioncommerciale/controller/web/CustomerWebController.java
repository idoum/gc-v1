/*
 * @path src/main/java/com/example/gestioncommerciale/controller/web/CustomerWebController.java
 * @description Contrôleur Web MVC pour l'interface utilisateur des clients avec HTMX
 */
package com.example.gestioncommerciale.controller.web;

import com.example.gestioncommerciale.dto.CustomerDTO;
import com.example.gestioncommerciale.model.Customer;
import com.example.gestioncommerciale.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerWebController {
    
    private final CustomerService customerService;
    
    /**
     * Liste paginée des clients avec recherche et filtres
     */
    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Customer.CustomerStatus status,
            @RequestParam(required = false) Customer.CustomerType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("Liste clients - search: {}, status: {}, type: {}, page: {}", search, status, type, page);
        
        // Créer le Pageable avec tri
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Récupérer les clients avec filtres
        Page<CustomerDTO> customers = customerService.findWithFilters(status, type, search, pageable);
        
        // Préparer le modèle
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        
        // Statistiques
        model.addAttribute("totalCustomers", customerService.countAll());
        model.addAttribute("activeCustomers", customerService.countActiveCustomers());
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "Clients", "url", "/customers")
        ));
        
        // Actions de page
        model.addAttribute("pageActions", List.of(
            Map.of("label", "Nouveau client", "icon", "bi-plus", "variant", "btn-primary", 
                   "type", "modal", "url", "/customers/new", "target", "#modal-container")
        ));
        
        // Si requête HTMX, retourner seulement le fragment de tableau
        if (isHtmxRequest(request)) {
            return "customers/list :: customer-table";
        }
        
        return "customers/list";
    }
    
    /**
     * Fragment de ligne client pour HTMX
     */
    @GetMapping("/_row/{id}")
    public String customerRow(@PathVariable Long id, Model model) {
        CustomerDTO customer = customerService.getById(id);
        model.addAttribute("customer", customer);
        return "customers/_customerRow :: customer-row";
    }
    
    /**
     * Recherche instantanée HTMX
     */
    @GetMapping("/search")
    public String searchCustomers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDTO> customers = customerService.search(q, pageable);
        
        model.addAttribute("customers", customers);
        model.addAttribute("search", q);
        
        return "customers/list :: customer-table";
    }
    
    /**
     * Modal de création d'un nouveau client
     */
    @GetMapping("/new")
    public String newCustomerModal(Model model) {
        model.addAttribute("customer", new CustomerDTO());
        model.addAttribute("isEdit", false);
        return "customers/_customerFormModal :: customer-form-modal";
    }
    
    /**
     * Modal d'édition d'un client existant
     */
    @GetMapping("/{id}/edit")
    public String editCustomerModal(@PathVariable Long id, Model model) {
        CustomerDTO customer = customerService.getById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("isEdit", true);
        return "customers/_customerFormModal :: customer-form-modal";
    }
    
    /**
     * Création d'un nouveau client (POST)
     */
    @PostMapping
    public String createCustomer(
            @Valid @ModelAttribute CustomerDTO customer,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la création du client: {}", bindingResult.getAllErrors());
            model.addAttribute("customer", customer);
            model.addAttribute("isEdit", false);
            
            if (isHtmxRequest(request)) {
                return "customers/_customerFormModal :: customer-form-modal";
            }
            return "customers/list";
        }
        
        try {
            CustomerDTO created = customerService.create(customer);
            log.info("Client créé avec succès: {}", created.getCode());
            
            if (isHtmxRequest(request)) {
                // Retourner le fragment de la nouvelle ligne + fermeture du modal
                model.addAttribute("customer", created);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Client " + created.getCompanyName() + " créé avec succès");
                
                // HX-Trigger pour fermer le modal et recharger la table
                request.setAttribute("HX-Trigger", "customerCreated");
                return "customers/_customerRow :: customer-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Client " + created.getCompanyName() + " créé avec succès");
            return "redirect:/customers";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du client", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                model.addAttribute("customer", customer);
                model.addAttribute("isEdit", false);
                return "customers/_customerFormModal :: customer-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/customers";
        }
    }
    
    /**
     * Mise à jour d'un client existant (PUT/POST)
     */
    @PostMapping("/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute CustomerDTO customer,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la mise à jour du client: {}", bindingResult.getAllErrors());
            customer.setId(id);
            model.addAttribute("customer", customer);
            model.addAttribute("isEdit", true);
            
            if (isHtmxRequest(request)) {
                return "customers/_customerFormModal :: customer-form-modal";
            }
            return "customers/list";
        }
        
        try {
            CustomerDTO updated = customerService.update(id, customer);
            log.info("Client mis à jour avec succès: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("customer", updated);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Client " + updated.getCompanyName() + " mis à jour avec succès");
                
                request.setAttribute("HX-Trigger", "customerUpdated");
                return "customers/_customerRow :: customer-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Client " + updated.getCompanyName() + " mis à jour avec succès");
            return "redirect:/customers";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du client", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                customer.setId(id);
                model.addAttribute("customer", customer);
                model.addAttribute("isEdit", true);
                return "customers/_customerFormModal :: customer-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/customers";
        }
    }
    
    /**
     * Suppression d'un client (DELETE)
     */
    @DeleteMapping("/{id}")
    public String deleteCustomer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            CustomerDTO customer = customerService.getById(id);
            customerService.delete(id);
            log.info("Client supprimé avec succès: {}", customer.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "customerDeleted");
                return ""; // Réponse vide pour suppression HTMX
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Client " + customer.getCompanyName() + " supprimé avec succès");
            return "redirect:/customers";
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du client", e);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "customerDeleteError");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/customers";
        }
    }
    
    /**
     * Vérifie si la requête vient de HTMX
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
    // Import ajouté pour les contacts
    private final com.example.gestioncommerciale.service.crm.ContactService contactService;

    /**
     * Onglet contacts pour un client
     */
    @GetMapping("/{id}/contacts")
    public String customerContacts(@PathVariable Long id, Model model, HttpServletRequest request) {
        CustomerDTO customer = customerService.getById(id);
        List<com.example.gestioncommerciale.dto.crm.ContactDTO> contacts = contactService.findByCustomer(id);

        model.addAttribute("customer", customer);
        model.addAttribute("contacts", contacts);

        if (isHtmxRequest(request)) {
            return "customers/_customerContacts :: customer-contacts-tab";
        }

        return "customers/detail";
    }

    /**
     * Fragment de liste des contacts d'un client
     */
    @GetMapping("/{id}/contacts/_list")
    public String customerContactsList(@PathVariable Long id, Model model) {
        List<com.example.gestioncommerciale.dto.crm.ContactDTO> contacts = contactService.findByCustomer(id);
        model.addAttribute("contacts", contacts);
        model.addAttribute("customerId", id);
        return "customers/_customerContactsList :: customer-contacts-list";
    }
}
