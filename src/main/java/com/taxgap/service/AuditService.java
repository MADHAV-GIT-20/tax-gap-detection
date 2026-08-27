package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.entity.AuditLog;
import com.taxgap.entity.Transaction;
import com.taxgap.enums.EventType;
import com.taxgap.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes an audit-trail row for every ingestion, tax computation and rule execution.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

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

    public List<AuditLog> find(String transactionId, EventType eventType) {
        if (transactionId != null && !transactionId.isBlank()) {
            return auditLogRepository.findByTransactionIdOrderByTimestampAsc(transactionId);
        }
        if (eventType != null) {
            return auditLogRepository.findByEventTypeOrderByTimestampDesc(eventType);
        }
        return auditLogRepository.findAll();
    }

    private void save(EventType eventType, String transactionId, Map<String, Object> detail) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setTransactionId(transactionId);
        log.setDetailJson(toJson(detail));
        auditLogRepository.save(log);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
