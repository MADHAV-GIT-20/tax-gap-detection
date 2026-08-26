package com.taxgap.service;

import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.repository.ExceptionRecordRepository;
import com.taxgap.repository.projection.CountByKeyProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final ExceptionRecordRepository exceptionRepository;

    @Transactional
    public List<ExceptionRecord> saveAll(List<ExceptionRecord> records) {
        return exceptionRepository.saveAll(records);
    }

    @Transactional(readOnly = true)
    public List<ExceptionRecord> search(String customerId, Severity severity, String ruleName) {
        return exceptionRepository.search(emptyToNull(customerId), severity, emptyToNull(ruleName));
    }

    @Transactional(readOnly = true)
    public ExceptionSummaryDto summary() {
        long total = exceptionRepository.count();

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            bySeverity.put(s.name(), exceptionRepository.countBySeverity(s));
        }

        Map<String, Long> byCustomer = new LinkedHashMap<>();
        for (CountByKeyProjection p : exceptionRepository.countByCustomer()) {
            byCustomer.put(p.getKey(), p.getCount());
        }

        return new ExceptionSummaryDto(total, bySeverity, byCustomer);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
