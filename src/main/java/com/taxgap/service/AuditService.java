package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.AuditLog;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.EventType;
import com.taxgap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes an audit-trail entry for every ingestion, tax computation and rule execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void logIngestion(String transactionId, String rawPayload) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("received", rawPayload);
        save(EventType.INGESTION, transactionId, detail);
    }

    public void logTaxComputation(Transaction txn) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("amount", txn.getAmount());
        detail.put("taxRate", txn.getTaxRate());
        detail.put("reportedTax", txn.getReportedTax());
        detail.put("expectedTax", txn.getExpectedTax());
        detail.put("taxGap", txn.getTaxGap());
        detail.put("complianceStatus", txn.getComplianceStatus());
        save(EventType.TAX_COMPUTATION, txn.getTransactionId(), detail);
    }

    public void logRuleExecution(String transactionId, String ruleName, boolean violated) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("ruleName", ruleName);
        detail.put("violated", violated);
        save(EventType.RULE_EXECUTION, transactionId, detail);
    }

    private void save(EventType eventType, String transactionId, Map<String, Object> detail) {
        auditLogRepository.save(AuditLog.builder()
                .eventType(eventType)
                .transactionId(transactionId)
                .detailJson(toJson(detail))
                .build());
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            log.warn("Failed to serialize audit detail", e);
            return "{}";
        }
    }
}
