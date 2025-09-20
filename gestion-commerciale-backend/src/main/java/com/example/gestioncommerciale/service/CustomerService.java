/*
 * @path src/main/java/com/example/gestioncommerciale/service/CustomerService.java
 * @description Service métier Customer
 */
package com.example.gestioncommerciale.service;

import com.example.gestioncommerciale.dto.AddressDTO;
import com.example.gestioncommerciale.dto.CustomerDTO;
import com.example.gestioncommerciale.mapper.AddressMapper;
import com.example.gestioncommerciale.mapper.CustomerMapper;
import com.example.gestioncommerciale.model.Address;
import com.example.gestioncommerciale.model.Customer;
import com.example.gestioncommerciale.repository.AddressRepository;
import com.example.gestioncommerciale.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;
    private final SequenceService sequenceService;

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByCode(String code) {
        return customerRepository.findByCode(code).map(customerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> search(String searchTerm, Pageable pageable) {
        return customerRepository.findBySearchTerm(searchTerm, pageable)
                .map(customerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findActiveCustomers() {
        return customerMapper.toDTOList(
                customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE));
    }

    public CustomerDTO create(CustomerDTO customerDTO) {
        if (customerDTO.getCode() == null || customerDTO.getCode().trim().isEmpty()) {
            customerDTO.setCode(sequenceService.generateCustomerCode());
        }

        validateUniqueFields(customerDTO, null);

        Customer customer = customerMapper.toEntity(customerDTO);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepository.save(customer);

        if (customerDTO.getAddresses() != null && !customerDTO.getAddresses().isEmpty()) {
            createAddresses(saved, customerDTO.getAddresses());
        }

        return customerMapper.toDTO(saved);
    }

    public CustomerDTO update(Long id, CustomerDTO customerDTO) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));

        validateUniqueFields(customerDTO, id);

        customerMapper.updateCustomerFromDTO(customerDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());

        Customer updated = customerRepository.save(existing);
        return customerMapper.toDTO(updated);
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));

        addressRepository.deleteByCustomerId(id);
        customerRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getCustomerAddresses(Long customerId) {
        return addressMapper.toDTOList(
                addressRepository.findByCustomerIdOrderByIsDefaultDesc(customerId));
    }

    public AddressDTO addAddress(Long customerId, AddressDTO addressDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + customerId));

        Address address = addressMapper.toEntity(addressDTO);
        address.setCustomer(customer);

        if (address.getIsDefault()) {
            addressRepository.unsetAllDefaultForCustomer(customerId);
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toDTO(saved);
    }

    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address existing = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + addressId));

        addressMapper.updateAddressFromDTO(addressDTO, existing);

        if (existing.getIsDefault()) {
            addressRepository.unsetAllDefaultForCustomer(
                    existing.getCustomer().getId());
            existing.setIsDefault(true);
        }

        Address updated = addressRepository.save(existing);
        return addressMapper.toDTO(updated);
    }

    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + addressId));

        addressRepository.delete(address);
    }

    public void setDefaultAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Adresse non trouvée avec ID: " + addressId));

        addressRepository.unsetAllDefaultForCustomer(address.getCustomer().getId());
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public long countByStatus(Customer.CustomerStatus status) {
        return customerRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return customerRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActiveCustomers() {
        return countByStatus(Customer.CustomerStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countCreatedToday() {
        return 0; // À implémenter si nécessaire
    }

    private void validateUniqueFields(CustomerDTO customerDTO, Long excludeId) {
        if (customerDTO.getCode() != null) {
            boolean codeExists = (excludeId == null)
                    ? customerRepository.existsByCode(customerDTO.getCode())
                    : customerRepository.existsByCodeAndIdNot(customerDTO.getCode(), excludeId);

            if (codeExists) {
                throw new IllegalArgumentException("Un client avec ce code existe déjà: " + customerDTO.getCode());
            }
        }

        if (customerDTO.getEmail() != null && !customerDTO.getEmail().trim().isEmpty()) {
            boolean emailExists = (excludeId == null)
                    ? customerRepository.existsByEmail(customerDTO.getEmail())
                    : customerRepository.existsByEmailAndIdNot(customerDTO.getEmail(), excludeId);

            if (emailExists) {
                throw new IllegalArgumentException("Un client avec cet email existe déjà: " + customerDTO.getEmail());
            }
        }
    }

    private void createAddresses(Customer customer, List<AddressDTO> addressDTOs) {
        for (AddressDTO addressDTO : addressDTOs) {
            Address address = addressMapper.toEntity(addressDTO);
            address.setCustomer(customer);
            addressRepository.save(address);
        }
    }

    // Dans CustomerService
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findWithFilters(Customer.CustomerStatus status,
            Customer.CustomerType type,
            String search,
            Pageable pageable) {
        return customerRepository.findWithFilters(status, type, search, pageable)
                .map(customerMapper::toDTO);
    }

    // Dans CustomerService
    @Transactional(readOnly = true)
    public CustomerDTO getById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));
    }

}
