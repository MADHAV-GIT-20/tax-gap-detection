package com.taxgap.repository;

import com.taxgap.entity.AuditLog;
import com.taxgap.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTransactionIdOrderByTimestampAsc(String transactionId);

    List<AuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType);
}
