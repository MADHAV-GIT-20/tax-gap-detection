package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.dto.BatchUploadRequest;
import com.taxgap.dto.BatchUploadResponse;
import com.taxgap.dto.TransactionDto;
import com.taxgap.entity.ExceptionRecord;
import com.taxgap.entity.Transaction;
import com.taxgap.enums.ComplianceStatus;
import com.taxgap.enums.TransactionType;
import com.taxgap.enums.ValidationStatus;
import com.taxgap.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Handles the whole upload pipeline for transactions:
 *   validate -> compute tax gap -> run rules -> save, with audit logging along the way.
 * Also provides read access to stored transactions.
 */
@Service
public class TransactionService {

    private static final BigDecimal TOLERANCE = BigDecimal.ONE; // |taxGap| <= 1 is compliant
    private static final int SCALE = 4;

    private final TransactionRepository transactionRepository;
    private final RuleService ruleService;
    private final ExceptionService exceptionService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionService(TransactionRepository transactionRepository,
                              RuleService ruleService,
                              ExceptionService exceptionService,
                              AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.ruleService = ruleService;
        this.exceptionService = exceptionService;
        this.auditService = auditService;
    }

    public BatchUploadResponse process(BatchUploadRequest request) {
        List<Transaction> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;
        int exceptionsRaised = 0;

        for (TransactionDto dto : request.getTransactions()) {
            // 1. ingestion audit
            auditService.logIngestion(dto.getTransactionId(), toJson(dto));

            // 2. validate
            Transaction txn = validate(dto);

            if (txn.getValidationStatus() == ValidationStatus.FAILURE) {
                transactionRepository.save(txn);
                failed++;
                results.add(txn);
                continue;
            }

            // 3. tax gap
            computeTaxGap(txn);
            auditService.logTaxComputation(txn);

            // save before running rules (the refund rule reads the customer's transactions)
            transactionRepository.save(txn);

            // 4. rules -> exceptions
            List<ExceptionRecord> exceptions = ruleService.runRules(txn);
            if (!exceptions.isEmpty()) {
                exceptionService.saveAll(exceptions);
                exceptionsRaised += exceptions.size();
            }

            succeeded++;
            results.add(txn);
        }

        BatchUploadResponse response = new BatchUploadResponse();
        response.setReceived(request.getTransactions().size());
        response.setSucceeded(succeeded);
        response.setFailed(failed);
        response.setExceptionsRaised(exceptionsRaised);
        response.setResults(results);
        return response;
    }

    public List<Transaction> findAll(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return transactionRepository.findAll();
        }
        return transactionRepository.findByCustomerId(customerId);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
    }

    // ---- validation ----

    private Transaction validate(TransactionDto dto) {
        List<String> failures = new ArrayList<>();
        Transaction txn = new Transaction();

        txn.setTransactionId(requireText(dto.getTransactionId(), "transactionId", failures));
        txn.setCustomerId(requireText(dto.getCustomerId(), "customerId", failures));

        // date
        if (isBlank(dto.getDate())) {
            failures.add("date is required");
        } else {
            try {
                txn.setDate(LocalDate.parse(dto.getDate().trim()));
            } catch (DateTimeParseException e) {
                failures.add("date has invalid format (expected yyyy-MM-dd): " + dto.getDate());
            }
        }

        // amount (required, numeric, > 0)
        if (isBlank(dto.getAmount())) {
            failures.add("amount is required");
        } else {
            BigDecimal amount = parseNumber(dto.getAmount(), "amount", failures);
            if (amount != null) {
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    failures.add("amount must be > 0");
                }
                txn.setAmount(amount);
            }
        }

        // taxRate (required, numeric)
        if (isBlank(dto.getTaxRate())) {
            failures.add("taxRate is required");
        } else {
            txn.setTaxRate(parseNumber(dto.getTaxRate(), "taxRate", failures));
        }

        // reportedTax (optional: if missing, compliance becomes NON_COMPLIANT)
        if (!isBlank(dto.getReportedTax())) {
            txn.setReportedTax(parseNumber(dto.getReportedTax(), "reportedTax", failures));
        }

        // transactionType (required, valid enum)
        if (isBlank(dto.getTransactionType())) {
            failures.add("transactionType is required");
        } else {
            try {
                txn.setTransactionType(TransactionType.valueOf(dto.getTransactionType().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                failures.add("transactionType is invalid (expected SALE/REFUND/EXPENSE): " + dto.getTransactionType());
            }
        }

        if (failures.isEmpty()) {
            txn.setValidationStatus(ValidationStatus.SUCCESS);
        } else {
            txn.setValidationStatus(ValidationStatus.FAILURE);
            txn.setFailureReasons(String.join("; ", failures));
        }
        return txn;
    }

    // ---- tax gap ----

    private void computeTaxGap(Transaction txn) {
        BigDecimal expectedTax = txn.getAmount().multiply(txn.getTaxRate()).setScale(SCALE, RoundingMode.HALF_UP);
        txn.setExpectedTax(expectedTax);

        if (txn.getReportedTax() == null) {
            txn.setTaxGap(null);
            txn.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
            return;
        }

        BigDecimal taxGap = expectedTax.subtract(txn.getReportedTax()).setScale(SCALE, RoundingMode.HALF_UP);
        txn.setTaxGap(taxGap);

        if (taxGap.abs().compareTo(TOLERANCE) <= 0) {
            txn.setComplianceStatus(ComplianceStatus.COMPLIANT);
        } else if (taxGap.compareTo(TOLERANCE) > 0) {
            txn.setComplianceStatus(ComplianceStatus.UNDERPAID);
        } else {
            txn.setComplianceStatus(ComplianceStatus.OVERPAID);
        }
    }

    // ---- small helpers ----

    private String requireText(String value, String field, List<String> failures) {
        if (isBlank(value)) {
            failures.add(field + " is required");
            return "UNKNOWN";
        }
        return value.trim();
    }

    private BigDecimal parseNumber(String raw, String field, List<String> failures) {
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
