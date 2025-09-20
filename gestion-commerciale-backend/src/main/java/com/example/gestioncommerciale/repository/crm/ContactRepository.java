/*
 * @path src/main/java/com/example/gestioncommerciale/repository/crm/ContactRepository.java
 * @description Repository Contact avec requêtes de recherche avancées CRM
 */
package com.example.gestioncommerciale.repository.crm;

import com.example.gestioncommerciale.model.Customer;
import com.example.gestioncommerciale.model.crm.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    Optional<Contact> findByCode(String code);
    
    List<Contact> findByCustomerOrderByIsPrimaryDescLastNameAscFirstNameAsc(Customer customer);
    
    List<Contact> findByCustomerIdOrderByIsPrimaryDescLastNameAscFirstNameAsc(Long customerId);
    
    List<Contact> findByStatusOrderByLastNameAscFirstNameAsc(Contact.ContactStatus status);
    
    List<Contact> findByTypeOrderByLastNameAscFirstNameAsc(Contact.ContactType type);
    
    @Query("SELECT c FROM Contact c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:customerId IS NULL OR c.customer.id = :customerId) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:isPrimary IS NULL OR c.isPrimary = :isPrimary) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "  LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.mobile) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.customer.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contact> findWithFilters(
        @Param("status") Contact.ContactStatus status,
        @Param("type") Contact.ContactType type,
        @Param("customerId") Long customerId,
        @Param("priority") Contact.ContactPriority priority,
        @Param("isPrimary") Boolean isPrimary,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("SELECT c FROM Contact c WHERE c.nextContactDate IS NOT NULL AND c.nextContactDate <= :date")
    List<Contact> findContactsDueForFollowup(@Param("date") LocalDateTime date);
    
    @Query("SELECT c FROM Contact c WHERE c.lastContactDate IS NULL OR c.lastContactDate <= :date")
    List<Contact> findContactsWithoutRecentContact(@Param("date") LocalDateTime date);
    
    @Query("SELECT c FROM Contact c WHERE c.customer.id = :customerId AND c.isPrimary = true")
    Optional<Contact> findPrimaryContactByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT c FROM Contact c WHERE c.isDecisionMaker = true")
    List<Contact> findDecisionMakers();
    
    @Query("SELECT c FROM Contact c WHERE c.birthDate IS NOT NULL AND " +
           "MONTH(c.birthDate) = MONTH(CURRENT_DATE) AND " +
           "DAY(c.birthDate) = DAY(CURRENT_DATE)")
    List<Contact> findTodaysBirthdays();
    
    @Query("SELECT c FROM Contact c WHERE c.birthDate IS NOT NULL AND " +
           "MONTH(c.birthDate) = :month")
    List<Contact> findBirthdaysByMonth(@Param("month") int month);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.customer = :customer")
    long countByCustomer(@Param("customer") Customer customer);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.status = :status")
    long countByStatus(@Param("status") Contact.ContactStatus status);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.type = :type")
    long countByType(@Param("type") Contact.ContactType type);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.nextContactDate IS NOT NULL AND c.nextContactDate <= CURRENT_TIMESTAMP")
    long countOverdueContacts();
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.lastContactDate IS NULL OR c.lastContactDate <= :date")
    long countStaleContacts(@Param("date") LocalDateTime date);
    
    boolean existsByCode(String code);
    
    boolean existsByEmail(String email);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    boolean existsByCustomerIdAndIsPrimaryTrue(Long customerId);
    
    @Query("SELECT c FROM Contact c WHERE c.customer.id = :customerId AND c.id != :contactId AND c.isPrimary = true")
    List<Contact> findOtherPrimaryContactsForCustomer(@Param("customerId") Long customerId, @Param("contactId") Long contactId);
}
