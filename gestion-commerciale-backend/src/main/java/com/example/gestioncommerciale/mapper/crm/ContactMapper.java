/*
 * @path src/main/java/com/example/gestioncommerciale/mapper/crm/ContactMapper.java
 * @description Mapper Contact avec MapStruct et calculs automatiques
 */
package com.example.gestioncommerciale.mapper.crm;

import com.example.gestioncommerciale.dto.crm.ContactDTO;
import com.example.gestioncommerciale.model.crm.Contact;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContactMapper {
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.companyName")
    @Mapping(target = "customerCode", source = "customer.code")
    @Mapping(target = "fullName", expression = "java(contact.getFullName())")
    @Mapping(target = "displayName", expression = "java(contact.getDisplayName())")
    @Mapping(target = "formattedAddress", expression = "java(contact.getFormattedAddress())")
    @Mapping(target = "hasAddress", expression = "java(contact.hasAddress())")
    @Mapping(target = "isOverdue", expression = "java(contact.isOverdue())")
    @Mapping(target = "daysSinceLastContact", expression = "java(contact.getDaysSinceLastContact())")
    ContactDTO toDTO(Contact contact);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contact toEntity(ContactDTO contactDTO);
    
    List<ContactDTO> toDTOList(List<Contact> contacts);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateContactFromDTO(ContactDTO dto, @MappingTarget Contact entity);
    
    @AfterMapping
    default void calculateFields(@MappingTarget ContactDTO dto) {
        dto.calculateFields();
    }
}
