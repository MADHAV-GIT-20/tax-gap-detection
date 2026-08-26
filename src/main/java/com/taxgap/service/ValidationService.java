package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.TransactionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates a raw transaction and maps it to a Transaction entity, recording
 * per-transaction failure reasons instead of throwing.
 */
@Service
public class ValidationService {

    public Transaction validateAndMap(TransactionDto dto) {
        List<String> failures = new ArrayList<>();

        Transaction.TransactionBuilder builder = Transaction.builder();

        // --- transactionId (required) ---
        if (isBlank(dto.transactionId())) {
            failures.add("transactionId is required");
        }
        builder.transactionId(orUnknown(dto.transactionId()));

        // --- customerId (required) ---
        if (isBlank(dto.customerId())) {
            failures.add("customerId is required");
        }
        builder.customerId(orUnknown(dto.customerId()));

        // --- date (required + valid format) ---
        if (isBlank(dto.date())) {
            failures.add("date is required");
        } else {
            try {
                builder.date(LocalDate.parse(dto.date().trim()));
            } catch (DateTimeParseException e) {
                failures.add("date has invalid format (expected yyyy-MM-dd): " + dto.date());
            }
        }

        // --- amount (required, numeric, > 0) ---
        BigDecimal amount = parseDecimal(dto.amount(), "amount", failures);
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                failures.add("amount must be > 0");
            }
            builder.amount(amount);
        } else if (isBlank(dto.amount())) {
            failures.add("amount is required");
        }

        // --- taxRate (required, numeric) ---
        BigDecimal taxRate = parseDecimal(dto.taxRate(), "taxRate", failures);
        if (taxRate != null) {
            builder.taxRate(taxRate);
        } else if (isBlank(dto.taxRate())) {
            failures.add("taxRate is required");
        }

        // --- reportedTax (OPTIONAL: missing -> handled as NON_COMPLIANT downstream) ---
        if (!isBlank(dto.reportedTax())) {
            BigDecimal reportedTax = parseDecimal(dto.reportedTax(), "reportedTax", failures);
            builder.reportedTax(reportedTax);
        }

        // --- transactionType (required, valid enum) ---
        if (isBlank(dto.transactionType())) {
            failures.add("transactionType is required");
        } else {
            try {
                builder.transactionType(TransactionType.valueOf(dto.transactionType().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                failures.add("transactionType is invalid (expected SALE/REFUND/EXPENSE): " + dto.transactionType());
            }
        }

        if (failures.isEmpty()) {
            builder.validationStatus(ValidationStatus.SUCCESS);
        } else {
            builder.validationStatus(ValidationStatus.FAILURE);
            builder.failureReasons(String.join("; ", failures));
        }
        return builder.build();
    }

    private BigDecimal parseDecimal(String raw, String field, List<String> failures) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            failures.add(field + " is not a valid number: " + raw);
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String orUnknown(String s) {
        return isBlank(s) ? "UNKNOWN" : s.trim();
    }
}
