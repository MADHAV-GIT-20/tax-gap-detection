package com.taxgap.dto;

import com.taxgap.domain.Transaction;

import java.math.BigDecimal;

/**
 * Per-transaction outcome returned from a batch upload.
 */
public record TransactionResultDto(
        String transactionId,
        String validationStatus,
        String complianceStatus,
        BigDecimal expectedTax,
        BigDecimal taxGap,
        String failureReasons
) {
    public static TransactionResultDto from(Transaction t) {
        return new TransactionResultDto(
                t.getTransactionId(),
                t.getValidationStatus() == null ? null : t.getValidationStatus().name(),
                t.getComplianceStatus() == null ? null : t.getComplianceStatus().name(),
                t.getExpectedTax(),
                t.getTaxGap(),
                t.getFailureReasons()
        );
    }
}
