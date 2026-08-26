package com.taxgap.dto;

import com.taxgap.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionView(
        Long id,
        String transactionId,
        LocalDate date,
        String customerId,
        BigDecimal amount,
        BigDecimal taxRate,
        BigDecimal reportedTax,
        String transactionType,
        String validationStatus,
        String failureReasons,
        BigDecimal expectedTax,
        BigDecimal taxGap,
        String complianceStatus,
        Instant createdAt
) {
    public static TransactionView from(Transaction t) {
        return new TransactionView(
                t.getId(),
                t.getTransactionId(),
                t.getDate(),
                t.getCustomerId(),
                t.getAmount(),
                t.getTaxRate(),
                t.getReportedTax(),
                t.getTransactionType() == null ? null : t.getTransactionType().name(),
                t.getValidationStatus() == null ? null : t.getValidationStatus().name(),
                t.getFailureReasons(),
                t.getExpectedTax(),
                t.getTaxGap(),
                t.getComplianceStatus() == null ? null : t.getComplianceStatus().name(),
                t.getCreatedAt()
        );
    }
}
