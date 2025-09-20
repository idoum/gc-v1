/*
 * @path src/main/java/com/example/gestioncommerciale/controller/web/crm/ContactWebController.java
 * @description Contrôleur Web MVC pour l'interface utilisateur des contacts CRM avec HTMX
 */
package com.example.gestioncommerciale.controller.web.crm;

import com.example.gestioncommerciale.dto.crm.ContactDTO;
import com.example.gestioncommerciale.model.crm.Contact;
import com.example.gestioncommerciale.service.CustomerService;
import com.example.gestioncommerciale.service.crm.ContactService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/crm/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactWebController {
    
    private final ContactService contactService;
    private final CustomerService customerService;
    
    /**
     * Liste paginée des contacts avec filtres CRM
     */
    @GetMapping
    public String listContacts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Contact.ContactStatus status,
            @RequestParam(required = false) Contact.ContactType type,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Contact.ContactPriority priority,
            @RequestParam(required = false) Boolean isPrimary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("Liste contacts - search: {}, status: {}, type: {}, customerId: {}, page: {}", 
                  search, status, type, customerId, page);
        
        // Créer le Pageable avec tri
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // Récupérer les contacts avec filtres
        Page<ContactDTO> contacts = contactService.findWithFilters(
            status, type, customerId, priority, isPrimary, search, pageable
        );
        
        // Préparer le modèle
        model.addAttribute("contacts", contacts);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("customerId", customerId);
        model.addAttribute("priority", priority);
        model.addAttribute("isPrimary", isPrimary);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction);
        
        // Données pour les filtres
        model.addAttribute("customers", customerService.findActiveCustomers());
        model.addAttribute("contactStatuses", Arrays.asList(Contact.ContactStatus.values()));
        model.addAttribute("contactTypes", Arrays.asList(Contact.ContactType.values()));
        model.addAttribute("contactPriorities", Arrays.asList(Contact.ContactPriority.values()));
        
        // Statistiques CRM
        model.addAttribute("totalContacts", contactService.countAll());
        model.addAttribute("activeContacts", contactService.countByStatus(Contact.ContactStatus.ACTIVE));
        model.addAttribute("leadsCount", contactService.countByType(Contact.ContactType.LEAD));
        model.addAttribute("overdueContacts", contactService.countOverdueContacts());
        model.addAttribute("staleContacts", contactService.countStaleContacts(30));
        
        // Alerts et suivis
        model.addAttribute("todaysBirthdays", contactService.findTodaysBirthdays());
        model.addAttribute("overdueFollowups", contactService.findContactsDueForFollowup());
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "CRM", "url", "/crm"),
            Map.of("label", "Contacts", "url", "/crm/contacts")
        ));
        
        // Actions de page
        model.addAttribute("pageActions", List.of(
            Map.of("label", "Nouveau contact", "icon", "bi-person-plus", "variant", "btn-primary", 
                   "type", "modal", "url", "/crm/contacts/new", "target", "#modal-container"),
            Map.of("label", "Import CSV", "icon", "bi-upload", "variant", "btn-outline-secondary", 
                   "type", "modal", "url", "/crm/contacts/import", "target", "#modal-container"),
            Map.of("label", "Export", "icon", "bi-download", "variant", "btn-outline-secondary", 
                   "type", "action", "url", "/crm/contacts/export")
        ));
        
        // Si requête HTMX, retourner seulement le fragment de tableau
        if (isHtmxRequest(request)) {
            return "crm/contacts/list :: contact-table";
        }
        
        return "crm/contacts/list";
    }
    
    /**
     * Vue détaillée d'un contact
     */
    @GetMapping("/{id}")
    public String viewContact(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.getById(id);
        model.addAttribute("contact", contact);
        
        // Autres contacts du même client
        List<ContactDTO> otherContacts = contactService.findByCustomer(contact.getCustomerId())
            .stream()
            .filter(c -> !c.getId().equals(id))
            .toList();
        model.addAttribute("otherContacts", otherContacts);
        
        // Breadcrumbs
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Accueil", "url", "/"),
            Map.of("label", "CRM", "url", "/crm"),
            Map.of("label", "Contacts", "url", "/crm/contacts"),
            Map.of("label", contact.getFullName(), "url", "/crm/contacts/" + id)
        ));
        
        return "crm/contacts/detail";
    }
    
    /**
     * Fragment de ligne contact pour HTMX
     */
    @GetMapping("/_row/{id}")
    public String contactRow(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.getById(id);
        model.addAttribute("contact", contact);
        return "crm/contacts/_contactRow :: contact-row";
    }
    
    /**
     * Recherche instantanée HTMX
     */
    @GetMapping("/search")
    public String searchContacts(
            @RequestParam String q,
            @RequestParam(required = false) Contact.ContactStatus status,
            @RequestParam(required = false) Contact.ContactType type,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName", "firstName"));
        Page<ContactDTO> contacts = contactService.findWithFilters(
            status, type, customerId, null, null, q, pageable
        );
        
        model.addAttribute("contacts", contacts);
        model.addAttribute("search", q);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("customerId", customerId);
        
        return "crm/contacts/list :: contact-table";
    }
    
    /**
     * Modal de création d'un nouveau contact
     */
    @GetMapping("/new")
    public String newContactModal(@RequestParam(required = false) Long customerId, Model model) {
        ContactDTO contact = new ContactDTO();
        if (customerId != null) {
            contact.setCustomerId(customerId);
        }
        
        model.addAttribute("contact", contact);
        model.addAttribute("isEdit", false);
        model.addAttribute("customers", customerService.findActiveCustomers());
        model.addAttribute("contactStatuses", Arrays.asList(Contact.ContactStatus.values()));
        model.addAttribute("contactTypes", Arrays.asList(Contact.ContactType.values()));
        model.addAttribute("contactPriorities", Arrays.asList(Contact.ContactPriority.values()));
        model.addAttribute("civilities", Arrays.asList(Contact.Civility.values()));
        model.addAttribute("languages", Arrays.asList(Contact.PreferredLanguage.values()));
        model.addAttribute("preferredContacts", Arrays.asList(Contact.PreferredContact.values()));
        
        return "crm/contacts/_contactFormModal :: contact-form-modal";
    }
    
    /**
     * Modal d'édition d'un contact existant
     */
    @GetMapping("/{id}/edit")
    public String editContactModal(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.getById(id);
        model.addAttribute("contact", contact);
        model.addAttribute("isEdit", true);
        model.addAttribute("customers", customerService.findActiveCustomers());
        model.addAttribute("contactStatuses", Arrays.asList(Contact.ContactStatus.values()));
        model.addAttribute("contactTypes", Arrays.asList(Contact.ContactType.values()));
        model.addAttribute("contactPriorities", Arrays.asList(Contact.ContactPriority.values()));
        model.addAttribute("civilities", Arrays.asList(Contact.Civility.values()));
        model.addAttribute("languages", Arrays.asList(Contact.PreferredLanguage.values()));
        model.addAttribute("preferredContacts", Arrays.asList(Contact.PreferredContact.values()));
        
        return "crm/contacts/_contactFormModal :: contact-form-modal";
    }
    
    /**
     * Création d'un nouveau contact (POST)
     */
    @PostMapping
    public String createContact(
            @Valid @ModelAttribute ContactDTO contact,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la création du contact: {}", bindingResult.getAllErrors());
            model.addAttribute("contact", contact);
            model.addAttribute("isEdit", false);
            addFormAttributes(model);
            
            if (isHtmxRequest(request)) {
                return "crm/contacts/_contactFormModal :: contact-form-modal";
            }
            return "crm/contacts/list";
        }
        
        try {
            ContactDTO created = contactService.create(contact);
            log.info("Contact créé avec succès: {}", created.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("contact", created);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Contact " + created.getFullName() + " créé avec succès");
                
                request.setAttribute("HX-Trigger", "contactCreated");
                return "crm/contacts/_contactRow :: contact-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contact " + created.getFullName() + " créé avec succès");
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du contact", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                model.addAttribute("contact", contact);
                model.addAttribute("isEdit", false);
                addFormAttributes(model);
                return "crm/contacts/_contactFormModal :: contact-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Mise à jour d'un contact existant (PUT/POST)
     */
    @PostMapping("/{id}")
    public String updateContact(
            @PathVariable Long id,
            @Valid @ModelAttribute ContactDTO contact,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            log.warn("Erreurs de validation lors de la mise à jour du contact: {}", bindingResult.getAllErrors());
            contact.setId(id);
            model.addAttribute("contact", contact);
            model.addAttribute("isEdit", true);
            addFormAttributes(model);
            
            if (isHtmxRequest(request)) {
                return "crm/contacts/_contactFormModal :: contact-form-modal";
            }
            return "crm/contacts/list";
        }
        
        try {
            ContactDTO updated = contactService.update(id, contact);
            log.info("Contact mis à jour avec succès: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                model.addAttribute("contact", updated);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Contact " + updated.getFullName() + " mis à jour avec succès");
                
                request.setAttribute("HX-Trigger", "contactUpdated");
                return "crm/contacts/_contactRow :: contact-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contact " + updated.getFullName() + " mis à jour avec succès");
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du contact", e);
            
            if (isHtmxRequest(request)) {
                model.addAttribute("errorMessage", "Erreur: " + e.getMessage());
                contact.setId(id);
                model.addAttribute("contact", contact);
                model.addAttribute("isEdit", true);
                addFormAttributes(model);
                return "crm/contacts/_contactFormModal :: contact-form-modal";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Suppression d'un contact (DELETE)
     */
    @DeleteMapping("/{id}")
    public String deleteContact(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ContactDTO contact = contactService.getById(id);
            contactService.delete(id);
            log.info("Contact supprimé avec succès: {}", contact.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "contactDeleted");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contact " + contact.getFullName() + " supprimé avec succès");
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du contact", e);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "contactDeleteError");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Définir un contact comme primaire
     */
    @PostMapping("/{id}/set-primary")
    public String setPrimaryContact(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ContactDTO updated = contactService.setPrimaryContact(id);
            log.info("Contact défini comme primaire: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "contactUpdated");
                return "crm/contacts/_contactRow :: contact-row";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contact " + updated.getFullName() + " défini comme primaire");
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la définition du contact primaire", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Modal de mise à jour du dernier contact
     */
    @GetMapping("/{id}/last-contact")
    public String lastContactModal(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.getById(id);
        model.addAttribute("contact", contact);
        return "crm/contacts/_lastContactModal :: last-contact-modal";
    }
    
    /**
     * Mise à jour du dernier contact (POST)
     */
    @PostMapping("/{id}/last-contact")
    public String updateLastContact(
            @PathVariable Long id,
            @RequestParam String note,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ContactDTO updated = contactService.updateLastContact(id, note);
            log.info("Dernier contact mis à jour pour: {}", updated.getCode());
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "contactUpdated");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Dernier contact mis à jour pour " + updated.getFullName());
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du dernier contact", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Modal de planification du prochain contact
     */
    @GetMapping("/{id}/next-contact")
    public String nextContactModal(@PathVariable Long id, Model model) {
        ContactDTO contact = contactService.getById(id);
        model.addAttribute("contact", contact);
        return "crm/contacts/_nextContactModal :: next-contact-modal";
    }
    
    /**
     * Planification du prochain contact (POST)
     */
    @PostMapping("/{id}/next-contact")
    public String scheduleNextContact(
            @PathVariable Long id,
            @RequestParam LocalDateTime nextContactDate,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            ContactDTO updated = contactService.scheduleNextContact(id, nextContactDate, note);
            log.info("Prochain contact programmé pour: {} le {}", updated.getCode(), nextContactDate);
            
            if (isHtmxRequest(request)) {
                request.setAttribute("HX-Trigger", "contactUpdated");
                return "";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Prochain contact programmé pour " + updated.getFullName());
            return "redirect:/crm/contacts";
            
        } catch (Exception e) {
            log.error("Erreur lors de la programmation du prochain contact", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }
    
    /**
     * Ajoute les attributs nécessaires aux formulaires
     */
    private void addFormAttributes(Model model) {
        model.addAttribute("customers", customerService.findActiveCustomers());
        model.addAttribute("contactStatuses", Arrays.asList(Contact.ContactStatus.values()));
        model.addAttribute("contactTypes", Arrays.asList(Contact.ContactType.values()));
        model.addAttribute("contactPriorities", Arrays.asList(Contact.ContactPriority.values()));
        model.addAttribute("civilities", Arrays.asList(Contact.Civility.values()));
        model.addAttribute("languages", Arrays.asList(Contact.PreferredLanguage.values()));
        model.addAttribute("preferredContacts", Arrays.asList(Contact.PreferredContact.values()));
    }
    
    /**
     * Vérifie si la requête vient de HTMX
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
