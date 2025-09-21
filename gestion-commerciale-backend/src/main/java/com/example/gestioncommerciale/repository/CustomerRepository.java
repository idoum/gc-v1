/*
 * @path src/main/java/com/example/gestioncommerciale/repository/CustomerRepository.java
 * @description Repository Customer avec requêtes de base
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

        Optional<Customer> findByCode(String code);

        Optional<Customer> findByEmail(String email);

        List<Customer> findByStatus(Customer.CustomerStatus status);

        @Query("SELECT c FROM Customer c WHERE " +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.contactFirstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.contactLastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))")
        Page<Customer> findBySearchTerm(@Param("search") String search, Pageable pageable);

        @Query(value = "SELECT c FROM Customer c WHERE " +
                        "(:status IS NULL OR c.status = :status) AND " +
                        "(:type IS NULL OR c.type = :type) AND (" +
                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.contactFirstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.contactLastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))" +
                        ")", countQuery = "SELECT COUNT(c) FROM Customer c WHERE " +
                                        "(:status IS NULL OR c.status = :status) AND " +
                                        "(:type IS NULL OR c.type = :type) AND (" +
                                        "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                                        "LOWER(c.contactFirstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                                        "LOWER(c.contactLastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                                        "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                                        "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))" +
                                        ")")
        Page<Customer> findWithFilters(
                        @Param("status") Customer.CustomerStatus status,
                        @Param("type") Customer.CustomerType type,
                        @Param("search") String search,
                        Pageable pageable);

        long countByStatus(Customer.CustomerStatus status);

        boolean existsByCode(String code);

        boolean existsByEmail(String email);

        boolean existsByCodeAndIdNot(String code, Long id);

        boolean existsByEmailAndIdNot(String email, Long id);
}
