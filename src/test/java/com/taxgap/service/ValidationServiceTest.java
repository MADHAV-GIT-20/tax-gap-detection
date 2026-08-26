package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.TransactionDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTest {

    private final ValidationService service = new ValidationService();

    private TransactionDto dto(String id, String date, String cust, String amount,
                               String rate, String reported, String type) {
        return new TransactionDto(id, date, cust, amount, rate, reported, type);
    }

    @Test
    void validTransactionPasses() {
        Transaction t = service.validateAndMap(
                dto("T1", "2026-01-15", "C1", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.SUCCESS);
        assertThat(t.getTransactionType()).isEqualTo(TransactionType.SALE);
        assertThat(t.getFailureReasons()).isNull();
    }

    @Test
    void missingRequiredFieldsAreReported() {
        Transaction t = service.validateAndMap(
                dto("", "2026-01-15", "", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("transactionId is required");
        assertThat(t.getFailureReasons()).contains("customerId is required");
    }

    @Test
    void invalidDateFormatIsRejected() {
        Transaction t = service.validateAndMap(
                dto("T1", "15-01-2026", "C1", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("date has invalid format");
    }

    @Test
    void nonPositiveAmountIsRejected() {
        Transaction t = service.validateAndMap(
                dto("T1", "2026-01-15", "C1", "0", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("amount must be > 0");
    }

    @Test
    void nonNumericAmountIsRejected() {
        Transaction t = service.validateAndMap(
                dto("T1", "2026-01-15", "C1", "abc", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("amount is not a valid number");
    }

    @Test
    void invalidTransactionTypeIsRejected() {
        Transaction t = service.validateAndMap(
                dto("T1", "2026-01-15", "C1", "1000", "0.18", "180", "GIFT"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("transactionType is invalid");
    }

    @Test
    void missingReportedTaxIsAllowed() {
        Transaction t = service.validateAndMap(
                dto("T1", "2026-01-15", "C1", "1000", "0.18", null, "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.SUCCESS);
        assertThat(t.getReportedTax()).isNull();
    }
}
