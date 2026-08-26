package com.taxgap.dto;

/**
 * Raw transaction as received in the upload payload.
 * Fields are Strings on purpose: this lets the validation layer record a
 * per-transaction failure (e.g. bad date/number format) instead of failing
 * deserialization of the whole batch.
 */
public record TransactionDto(
        String transactionId,
        String date,
        String customerId,
        String amount,
        String taxRate,
        String reportedTax,
        String transactionType
) {
}
