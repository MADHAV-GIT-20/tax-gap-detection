package com.taxgap.repository;

import com.taxgap.domain.AuditLog;
import com.taxgap.domain.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTransactionIdOrderByTimestampAsc(String transactionId);

    List<AuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType);
}
