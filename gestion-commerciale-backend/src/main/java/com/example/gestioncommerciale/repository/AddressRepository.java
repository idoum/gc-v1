/*
 * @path src/main/java/com/example/gestioncommerciale/repository/AddressRepository.java
 * @description Repository Address pour gestion des adresses
 */
package com.example.gestioncommerciale.repository;

import com.example.gestioncommerciale.model.Address;
import com.example.gestioncommerciale.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByCustomerId(Long customerId);
    
    List<Address> findByCustomerIdOrderByIsDefaultDesc(Long customerId);
    
    List<Address> findByCustomerAndType(Customer customer, Address.AddressType type);
    
    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long customerId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void unsetAllDefaultForCustomer(@Param("customerId") Long customerId);
    
    void deleteByCustomerId(Long customerId);
}
