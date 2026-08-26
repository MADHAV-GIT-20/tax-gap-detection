package com.taxgap.dto;

import com.taxgap.domain.ExceptionRecord;

import java.time.Instant;

public record ExceptionView(
        Long id,
        String transactionId,
        String customerId,
        String ruleName,
        String severity,
        String message,
        Instant timestamp
) {
    public static ExceptionView from(ExceptionRecord e) {
        return new ExceptionView(
                e.getId(),
                e.getTransactionId(),
                e.getCustomerId(),
                e.getRuleName(),
                e.getSeverity() == null ? null : e.getSeverity().name(),
                e.getMessage(),
                e.getTimestamp()
        );
    }
}
