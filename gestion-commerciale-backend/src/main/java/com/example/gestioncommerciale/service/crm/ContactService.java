/*
 * @path src/main/java/com/example/gestioncommerciale/service/crm/ContactService.java
 * @description Service métier Contact avec logique CRM complète
 */
package com.example.gestioncommerciale.service.crm;

import com.example.gestioncommerciale.dto.crm.ContactDTO;
import com.example.gestioncommerciale.mapper.crm.ContactMapper;
import com.example.gestioncommerciale.model.Customer;
import com.example.gestioncommerciale.model.crm.Contact;
import com.example.gestioncommerciale.repository.CustomerRepository;
import com.example.gestioncommerciale.repository.crm.ContactRepository;
import com.example.gestioncommerciale.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactService {
    
    private final ContactRepository contactRepository;
    private final CustomerRepository customerRepository;
    private final ContactMapper contactMapper;
    private final SequenceService sequenceService;
    
    public static final String CONTACT_TYPE = "CONTACT";
    public static final String CONTACT_PREFIX = "CNT";
    
    @Transactional(readOnly = true)
    public Optional<ContactDTO> findById(Long id) {
        return contactRepository.findById(id)
            .map(contactMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public ContactDTO getById(Long id) {
        return contactRepository.findById(id)
            .map(contactMapper::toDTO)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Optional<ContactDTO> findByCode(String code) {
        return contactRepository.findByCode(code)
            .map(contactMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findByCustomer(Long customerId) {
        return contactRepository.findByCustomerIdOrderByIsPrimaryDescLastNameAscFirstNameAsc(customerId)
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Optional<ContactDTO> findPrimaryContactByCustomer(Long customerId) {
        return contactRepository.findPrimaryContactByCustomerId(customerId)
            .map(contactMapper::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findActiveContacts() {
        return contactRepository.findByStatusOrderByLastNameAscFirstNameAsc(Contact.ContactStatus.ACTIVE)
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<ContactDTO> findWithFilters(
            Contact.ContactStatus status,
            Contact.ContactType type,
            Long customerId,
            Contact.ContactPriority priority,
            Boolean isPrimary,
            String search,
            Pageable pageable
    ) {
        Page<Contact> contacts = contactRepository.findWithFilters(
            status, type, customerId, priority, isPrimary, search, pageable
        );
        
        List<ContactDTO> dtos = contacts.getContent()
            .stream()
            .map(contactMapper::toDTO)
            .toList();
        
        return new PageImpl<>(dtos, pageable, contacts.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findContactsDueForFollowup() {
        return contactRepository.findContactsDueForFollowup(LocalDateTime.now())
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findStaleContacts(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(daysThreshold, ChronoUnit.DAYS);
        return contactRepository.findContactsWithoutRecentContact(cutoffDate)
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findDecisionMakers() {
        return contactRepository.findDecisionMakers()
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findTodaysBirthdays() {
        return contactRepository.findTodaysBirthdays()
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ContactDTO> findBirthdaysByMonth(int month) {
        return contactRepository.findBirthdaysByMonth(month)
            .stream()
            .map(contactMapper::toDTO)
            .toList();
    }
    
    public ContactDTO create(ContactDTO contactDTO) {
        if (contactDTO.getCode() == null || contactDTO.getCode().trim().isEmpty()) {
            contactDTO.setCode(sequenceService.generateSequentialCode(CONTACT_TYPE, CONTACT_PREFIX));
        }
        
        validateUniqueFields(contactDTO, null);
        validateBusinessRules(contactDTO, null);
        
        Contact contact = contactMapper.toEntity(contactDTO);
        
        // Gérer la relation client
        Customer customer = customerRepository.findById(contactDTO.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + contactDTO.getCustomerId()));
        contact.setCustomer(customer);
        
        // Gérer le contact primaire
        if (contactDTO.getIsPrimary()) {
            clearOtherPrimaryContacts(contactDTO.getCustomerId(), null);
        }
        
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        
        Contact saved = contactRepository.save(contact);
        log.info("Contact créé avec succès: {} pour le client {}", saved.getCode(), customer.getCompanyName());
        
        return contactMapper.toDTO(saved);
    }
    
    public ContactDTO update(Long id, ContactDTO contactDTO) {
        Contact existing = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
        
        validateUniqueFields(contactDTO, id);
        validateBusinessRules(contactDTO, id);
        
        // Gérer le changement de client
        if (!existing.getCustomer().getId().equals(contactDTO.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(contactDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + contactDTO.getCustomerId()));
            existing.setCustomer(newCustomer);
        }
        
        // Gérer le contact primaire
        if (contactDTO.getIsPrimary() && !existing.getIsPrimary()) {
            clearOtherPrimaryContacts(contactDTO.getCustomerId(), id);
        }
        
        contactMapper.updateContactFromDTO(contactDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        
        Contact updated = contactRepository.save(existing);
        log.info("Contact mis à jour avec succès: {}", updated.getCode());
        
        return contactMapper.toDTO(updated);
    }
    
    public void delete(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
        
        // Vérifications avant suppression
        if (contact.getIsPrimary()) {
            long otherContacts = contactRepository.countByCustomerId(contact.getCustomer().getId()) - 1;
            if (otherContacts > 0) {
                throw new IllegalArgumentException(
                    "Impossible de supprimer le contact primaire. Désignez d'abord un autre contact comme primaire."
                );
            }
        }
        
        contactRepository.delete(contact);
        log.info("Contact supprimé avec succès: {}", contact.getCode());
    }
    
    public ContactDTO updateLastContact(Long id, String note) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
        
        contact.setLastContactDate(LocalDateTime.now());
        contact.setLastContactNote(note);
        contact.setUpdatedAt(LocalDateTime.now());
        
        Contact updated = contactRepository.save(contact);
        log.info("Dernier contact mis à jour pour: {}", updated.getCode());
        
        return contactMapper.toDTO(updated);
    }
    
    public ContactDTO scheduleNextContact(Long id, LocalDateTime nextContactDate, String note) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
        
        contact.setNextContactDate(nextContactDate);
        if (note != null) {
            contact.setLastContactNote(note);
        }
        contact.setUpdatedAt(LocalDateTime.now());
        
        Contact updated = contactRepository.save(contact);
        log.info("Prochain contact programmé pour: {} le {}", updated.getCode(), nextContactDate);
        
        return contactMapper.toDTO(updated);
    }
    
    public ContactDTO setPrimaryContact(Long id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact non trouvé avec ID: " + id));
        
        // Retirer le statut primaire des autres contacts du même client
        clearOtherPrimaryContacts(contact.getCustomer().getId(), id);
        
        contact.setIsPrimary(true);
        contact.setUpdatedAt(LocalDateTime.now());
        
        Contact updated = contactRepository.save(contact);
        log.info("Contact défini comme primaire: {}", updated.getCode());
        
        return contactMapper.toDTO(updated);
    }
    
    @Transactional(readOnly = true)
    public long countAll() {
        return contactRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long countByStatus(Contact.ContactStatus status) {
        return contactRepository.countByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public long countByType(Contact.ContactType type) {
        return contactRepository.countByType(type);
    }
    
    @Transactional(readOnly = true)
    public long countOverdueContacts() {
        return contactRepository.countOverdueContacts();
    }
    
    @Transactional(readOnly = true)
    public long countStaleContacts(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(daysThreshold, ChronoUnit.DAYS);
        return contactRepository.countStaleContacts(cutoffDate);
    }
    
    @Transactional(readOnly = true)
    public long countByCustomer(Long customerId) {
        return contactRepository.countByCustomerId(customerId);
    }
    
    private void validateUniqueFields(ContactDTO contactDTO, Long excludeId) {
        if (contactDTO.getCode() != null) {
            boolean codeExists = (excludeId == null) 
                ? contactRepository.existsByCode(contactDTO.getCode())
                : contactRepository.existsByCodeAndIdNot(contactDTO.getCode(), excludeId);
                
            if (codeExists) {
                throw new IllegalArgumentException("Un contact avec ce code existe déjà: " + contactDTO.getCode());
            }
        }
        
        if (contactDTO.getEmail() != null && !contactDTO.getEmail().trim().isEmpty()) {
            boolean emailExists = (excludeId == null) 
                ? contactRepository.existsByEmail(contactDTO.getEmail())
                : contactRepository.existsByEmailAndIdNot(contactDTO.getEmail(), excludeId);
                
            if (emailExists) {
                throw new IllegalArgumentException("Un contact avec cet email existe déjà: " + contactDTO.getEmail());
            }
        }
    }
    
    private void validateBusinessRules(ContactDTO contactDTO, Long excludeId) {
        // Validation email obligatoire pour les contacts de type LEAD
        if (contactDTO.getType() == Contact.ContactType.LEAD) {
            if (contactDTO.getEmail() == null || contactDTO.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email est obligatoire pour les prospects (LEAD)");
            }
        }
        
        // Validation téléphone obligatoire pour les contacts prioritaires
        if (contactDTO.getPriority() == Contact.ContactPriority.CRITICAL) {
            if ((contactDTO.getPhone() == null || contactDTO.getPhone().trim().isEmpty()) &&
                (contactDTO.getMobile() == null || contactDTO.getMobile().trim().isEmpty())) {
                throw new IllegalArgumentException("Un numéro de téléphone est obligatoire pour les contacts critiques");
            }
        }
        
        // Validation cohérence dates
        if (contactDTO.getNextContactDate() != null && contactDTO.getLastContactDate() != null) {
            if (contactDTO.getNextContactDate().isBefore(contactDTO.getLastContactDate())) {
                throw new IllegalArgumentException("La prochaine date de contact ne peut pas être antérieure à la dernière");
            }
        }
    }
    
    private void clearOtherPrimaryContacts(Long customerId, Long excludeContactId) {
        List<Contact> otherPrimaryContacts = (excludeContactId == null) 
            ? contactRepository.findByCustomerIdOrderByIsPrimaryDescLastNameAscFirstNameAsc(customerId)
                .stream().filter(Contact::getIsPrimary).toList()
            : contactRepository.findOtherPrimaryContactsForCustomer(customerId, excludeContactId);
        
        otherPrimaryContacts.forEach(contact -> {
            contact.setIsPrimary(false);
            contact.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(contact);
        });
    }
}
