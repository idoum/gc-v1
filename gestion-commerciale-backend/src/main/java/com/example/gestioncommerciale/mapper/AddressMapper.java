/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/AddressMapper.java
 * @description Mapper Address avec MapStruct
 */
package com.example.gestioncommerciale.mapper;

import com.example.gestioncommerciale.dto.AddressDTO;
import com.example.gestioncommerciale.model.Address;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {
    
    @Mapping(target = "customerId", source = "customer.id")
    AddressDTO toDTO(Address address);
    
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressDTO addressDTO);
    
    List<AddressDTO> toDTOList(List<Address> addresses);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    void updateAddressFromDTO(AddressDTO dto, @MappingTarget Address entity);
}
