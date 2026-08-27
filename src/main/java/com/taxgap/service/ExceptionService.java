package com.taxgap.service;

import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.entity.ExceptionRecord;
import com.taxgap.enums.Severity;
import com.taxgap.repository.ExceptionRecordRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExceptionService {

    private final ExceptionRecordRepository exceptionRepository;

    public ExceptionService(ExceptionRecordRepository exceptionRepository) {
        this.exceptionRepository = exceptionRepository;
    }

    public void saveAll(List<ExceptionRecord> records) {
        exceptionRepository.saveAll(records);
    }

    public List<ExceptionRecord> search(String customerId, Severity severity, String ruleName) {
        return exceptionRepository.search(blankToNull(customerId), severity, blankToNull(ruleName));
    }

    public ExceptionSummaryDto summary() {
        ExceptionSummaryDto dto = new ExceptionSummaryDto();
        dto.setTotalExceptions(exceptionRepository.count());

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            bySeverity.put(severity.name(), exceptionRepository.countBySeverity(severity));
        }
        dto.setCountBySeverity(bySeverity);

        Map<String, Long> byCustomer = new LinkedHashMap<>();
        for (Object[] row : exceptionRepository.countByCustomer()) {
            String customerId = (String) row[0];
            Long count = (Long) row[1];
            byCustomer.put(customerId, count);
        }
        dto.setCountByCustomer(byCustomer);

        return dto;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
