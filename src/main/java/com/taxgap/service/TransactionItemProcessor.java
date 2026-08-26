package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.TransactionDto;
import com.taxgap.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Processes a single transaction in its own database transaction so that one
 * bad record cannot roll back the rest of the batch. Kept in a separate bean
 * from {@link TransactionProcessingService} so the {@code @Transactional}
 * boundary is applied via the Spring proxy (self-invocation would bypass it).
 */
@Service
@RequiredArgsConstructor
public class TransactionItemProcessor {

    private final ValidationService validationService;
    private final TaxGapService taxGapService;
    private final RuleEngineService ruleEngineService;
    private final ExceptionService exceptionService;
    private final AuditService auditService;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProcessOutcome processOne(TransactionDto dto) {
        // [1] INGEST + audit
        auditService.logIngestion(dto.transactionId(), toJson(dto));

        // [2] VALIDATE
        Transaction txn = validationService.validateAndMap(dto);
        txn.setRawPayload(toJson(dto));

        if (txn.getValidationStatus() == ValidationStatus.FAILURE) {
            transactionRepository.save(txn);
            return new ProcessOutcome(txn, 0);
        }

        // [3] TAX GAP + audit
        taxGapService.compute(txn);
        auditService.logTaxComputation(txn);

        // persist before rule engine (rule context queries by customer)
        txn = transactionRepository.save(txn);

        // [4] RULE ENGINE (+ audit per rule inside the engine)
        List<ExceptionRecord> exceptions = ruleEngineService.runRules(txn);
        if (!exceptions.isEmpty()) {
            exceptionService.saveAll(exceptions);
        }
        return new ProcessOutcome(txn, exceptions.size());
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** Carrier for a single transaction's processing result. */
    public record ProcessOutcome(Transaction transaction, int exceptionCount) {
    }
}
