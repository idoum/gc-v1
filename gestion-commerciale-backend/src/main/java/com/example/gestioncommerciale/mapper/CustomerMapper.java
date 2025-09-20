/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/CustomerMapper.java
 * @description Mapper Customer avec MapStruct
 */
package com.example.gestioncommerciale.mapper;

import com.example.gestioncommerciale.dto.CustomerDTO;
import com.example.gestioncommerciale.model.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {AddressMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerMapper {
    
    CustomerDTO toDTO(Customer customer);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Customer toEntity(CustomerDTO customerDTO);
    
    List<CustomerDTO> toDTOList(List<Customer> customers);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateCustomerFromDTO(CustomerDTO dto, @MappingTarget Customer entity);
}
