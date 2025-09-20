/*
 * @path src/main/java/com/example/gestioncommerciale/service/SequenceService.java
 * @description Service de génération de codes séquentiels
 */
package com.example.gestioncommerciale.service;

import com.example.gestioncommerciale.model.Sequence;
import com.example.gestioncommerciale.repository.SequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceService {
    
    private final SequenceRepository sequenceRepository;
    
    public static final String CUSTOMER_TYPE = "CUSTOMER";
    public static final String CUSTOMER_PREFIX = "CLI";
    
    @Transactional
    public String generateCustomerCode() {
        return generateSequentialCode(CUSTOMER_TYPE, CUSTOMER_PREFIX);
    }
    
    @Transactional
    public String generateSequentialCode(String type, String prefix) {
        int currentYear = LocalDate.now().getYear();
        String yearStr = String.valueOf(currentYear);
        
        Sequence sequence = getOrCreateSequence(type, currentYear, prefix);
        sequence.setCurrentValue(sequence.getCurrentValue() + 1);
        sequence = sequenceRepository.save(sequence);
        
        String formattedNumber = String.format("%04d", sequence.getCurrentValue());
        String code = String.format("%s-%s-%s", prefix, yearStr, formattedNumber);
        
        log.debug("Code généré pour {} : {}", type, code);
        return code;
    }
    
    private Sequence getOrCreateSequence(String type, int year, String prefix) {
        Optional<Sequence> existing = sequenceRepository.findByTypeAndYear(type, year);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Sequence newSequence = new Sequence();
        newSequence.setType(type);
        newSequence.setYear(year);
        newSequence.setPrefix(prefix);
        newSequence.setCurrentValue(0L);
        newSequence.setPaddingLength(4);
        
        return sequenceRepository.save(newSequence);
    }
    public static final String CATEGORY_TYPE = "CATEGORY";
    public static final String CATEGORY_PREFIX = "CAT";

    public static final String CONTACT_TYPE = "CONTACT";
    public static final String CONTACT_PREFIX = "CNT";
    
    @Transactional
    public String generateCategoryCode() {
        return generateSequentialCode(CATEGORY_TYPE, CATEGORY_PREFIX);
    }
    
    @Transactional
    public String generateContactCode() {
        return generateSequentialCode(CONTACT_TYPE, CONTACT_PREFIX);
    }

}
