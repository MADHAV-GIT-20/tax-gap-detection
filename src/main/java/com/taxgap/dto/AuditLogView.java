package com.taxgap.dto;

import com.taxgap.domain.AuditLog;

import java.time.Instant;

public record AuditLogView(
        Long id,
        String eventType,
        String transactionId,
        Instant timestamp,
        String detailJson
) {
    public static AuditLogView from(AuditLog a) {
        return new AuditLogView(
                a.getId(),
                a.getEventType() == null ? null : a.getEventType().name(),
                a.getTransactionId(),
                a.getTimestamp(),
                a.getDetailJson()
        );
    }
}
