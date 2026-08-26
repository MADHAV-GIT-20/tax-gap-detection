package com.taxgap.service;

import com.taxgap.domain.AuditLog;
import com.taxgap.domain.enums.EventType;
import com.taxgap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLog> find(String transactionId, EventType eventType) {
        if (transactionId != null && !transactionId.isBlank()) {
            return auditLogRepository.findByTransactionIdOrderByTimestampAsc(transactionId);
        }
        if (eventType != null) {
            return auditLogRepository.findByEventTypeOrderByTimestampDesc(eventType);
        }
        return auditLogRepository.findAll();
    }
}
