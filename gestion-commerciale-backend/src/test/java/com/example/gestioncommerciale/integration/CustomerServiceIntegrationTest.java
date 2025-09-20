/*
 * @path src/test/java/com/example/gestioncommerciale/integration/CustomerServiceIntegrationTest.java
 * @description Tests d'intégration simples pour CustomerService
 */
package com.example.gestioncommerciale.integration;

import com.example.gestioncommerciale.dto.AddressDTO;
import com.example.gestioncommerciale.dto.CustomerDTO;
import com.example.gestioncommerciale.model.Address;
import com.example.gestioncommerciale.model.Customer;
import com.example.gestioncommerciale.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Test
    @DisplayName("Doit créer un client avec code généré automatiquement")
    void shouldCreateCustomerWithGeneratedCode() {
        // Given
        CustomerDTO customerDTO = createBasicCustomer();
        customerDTO.setCode(null); // Laisse le service générer le code

        // When
        CustomerDTO result = customerService.create(customerDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCode()).matches("CLI-\\d{4}-\\d{4}");
        assertThat(result.getCompanyName()).isEqualTo("Test Company");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Customer.CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("Doit lever une exception si code client déjà existant")
    void shouldThrowExceptionWhenDuplicateCode() {
        // Given
        CustomerDTO customer1 = createBasicCustomer();
        customer1.setCode("DUP-001");
        customer1.setEmail("test1@company.com");
        
        CustomerDTO customer2 = createBasicCustomer();
        customer2.setCode("DUP-001");
        customer2.setEmail("test2@company.com");
        
        customerService.create(customer1);

        // When / Then
        assertThatThrownBy(() -> customerService.create(customer2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Un client avec ce code existe déjà");
    }

    @Test
    @DisplayName("Doit rechercher un client par code")
    void shouldFindCustomerByCode() {
        // Given
        CustomerDTO customer = createBasicCustomer();
        customer.setCode("SEARCH-001");
        customerService.create(customer);

        // When
        Optional<CustomerDTO> found = customerService.findByCode("SEARCH-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCompanyName()).isEqualTo("Test Company");
    }

    @Test
    @DisplayName("Doit ajouter une adresse à un client")
    void shouldAddAddressToCustomer() {
        // Given
        CustomerDTO customer = createBasicCustomer();
        customer.setCode("ADDR-001");
        CustomerDTO created = customerService.create(customer);
        
        AddressDTO address = createBasicAddress();

        // When
        AddressDTO addedAddress = customerService.addAddress(created.getId(), address);

        // Then
        assertThat(addedAddress.getId()).isNotNull();
        assertThat(addedAddress.getCustomerId()).isEqualTo(created.getId());
    }

    private CustomerDTO createBasicCustomer() {
        CustomerDTO customer = new CustomerDTO();
        customer.setCompanyName("Test Company");
        customer.setContactFirstName("John");
        customer.setContactLastName("Doe");
        customer.setEmail("test@company.com");
        customer.setPhone("01.23.45.67.89");
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setType(Customer.CustomerType.COMPANY);
        customer.setPaymentTermDays(30);
        return customer;
    }
    
    private AddressDTO createBasicAddress() {
        AddressDTO address = new AddressDTO();
        address.setType(Address.AddressType.BOTH);
        address.setStreet1("123 Rue de Test");
        address.setZipCode("75001");
        address.setCity("Paris");
        address.setCountryCode("FR");
        address.setIsDefault(true);
        address.setActive(true);
        return address;
    }
}
