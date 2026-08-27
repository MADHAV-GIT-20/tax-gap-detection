package com.taxgap.service;

import com.taxgap.dto.BatchUploadRequest;
import com.taxgap.dto.BatchUploadResponse;
import com.taxgap.dto.TransactionDto;
import com.taxgap.entity.Transaction;
import com.taxgap.enums.ComplianceStatus;
import com.taxgap.enums.ValidationStatus;
import com.taxgap.support.FakeAuditLogRepository;
import com.taxgap.support.FakeExceptionRecordRepository;
import com.taxgap.support.FakeRuleRepository;
import com.taxgap.support.FakeTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionServiceTest {

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        FakeTransactionRepository txnRepo = new FakeTransactionRepository();
        FakeRuleRepository ruleRepo = new FakeRuleRepository();          // no rules -> focus on validation/tax
        FakeExceptionRecordRepository excRepo = new FakeExceptionRecordRepository();
        FakeAuditLogRepository auditRepo = new FakeAuditLogRepository();

        AuditService auditService = new AuditService(auditRepo);
        ExceptionService exceptionService = new ExceptionService(excRepo);
        RuleService ruleService = new RuleService(ruleRepo, txnRepo, auditService);
        transactionService = new TransactionService(txnRepo, ruleService, exceptionService, auditService);
    }

    private TransactionDto dto(String id, String date, String cust, String amount,
                               String rate, String reported, String type) {
        TransactionDto d = new TransactionDto();
        d.setTransactionId(id);
        d.setDate(date);
        d.setCustomerId(cust);
        d.setAmount(amount);
        d.setTaxRate(rate);
        d.setReportedTax(reported);
        d.setTransactionType(type);
        return d;
    }

    private Transaction processOne(TransactionDto dto) {
        BatchUploadRequest request = new BatchUploadRequest();
        request.setTransactions(List.of(dto));
        BatchUploadResponse response = transactionService.process(request);
        return response.getResults().get(0);
    }

    @Test
    void compliantTransaction() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.SUCCESS);
        assertThat(t.getExpectedTax()).isEqualByComparingTo("180.0000");
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void underpaidTransaction() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "1000", "0.18", "150", "SALE"));
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.UNDERPAID);
        assertThat(t.getTaxGap()).isEqualByComparingTo("30.0000");
    }

    @Test
    void overpaidTransaction() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "1000", "0.18", "200", "SALE"));
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.OVERPAID);
    }

    @Test
    void nonCompliantWhenReportedTaxMissing() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "1000", "0.18", null, "SALE"));
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.NON_COMPLIANT);
        assertThat(t.getTaxGap()).isNull();
    }

    @Test
    void invalidDateIsFailure() {
        Transaction t = processOne(dto("T1", "10-01-2026", "C1", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("date has invalid format");
    }

    @Test
    void nonPositiveAmountIsFailure() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "0", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("amount must be > 0");
    }

    @Test
    void invalidTransactionTypeIsFailure() {
        Transaction t = processOne(dto("T1", "2026-01-10", "C1", "1000", "0.18", "180", "GIFT"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("transactionType is invalid");
    }

    @Test
    void missingRequiredFieldsAreFailure() {
        Transaction t = processOne(dto("", "2026-01-10", "", "1000", "0.18", "180", "SALE"));
        assertThat(t.getValidationStatus()).isEqualTo(ValidationStatus.FAILURE);
        assertThat(t.getFailureReasons()).contains("transactionId is required");
        assertThat(t.getFailureReasons()).contains("customerId is required");
    }

    @Test
    void batchCountsSucceededAndFailed() {
        BatchUploadRequest request = new BatchUploadRequest();
        request.setTransactions(List.of(
                dto("T1", "2026-01-10", "C1", "1000", "0.18", "180", "SALE"),   // ok
                dto("T2", "bad", "C1", "1000", "0.18", "180", "SALE")));         // bad date
        BatchUploadResponse response = transactionService.process(request);
        assertThat(response.getReceived()).isEqualTo(2);
        assertThat(response.getSucceeded()).isEqualTo(1);
        assertThat(response.getFailed()).isEqualTo(1);
    }
}
