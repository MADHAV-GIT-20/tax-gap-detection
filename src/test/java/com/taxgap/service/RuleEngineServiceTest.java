package com.taxgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.Severity;
import com.taxgap.domain.enums.TransactionType;
import com.taxgap.repository.RuleRepository;
import com.taxgap.repository.TransactionRepository;
import com.taxgap.service.rules.GstSlabViolationEvaluator;
import com.taxgap.service.rules.HighValueEvaluator;
import com.taxgap.service.rules.RefundValidationEvaluator;
import com.taxgap.service.rules.RuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private RuleRepository ruleRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AuditService auditService;

    private RuleEngineService ruleEngineService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        List<RuleEvaluator> evaluators = List.of(
                new HighValueEvaluator(mapper),
                new RefundValidationEvaluator(),
                new GstSlabViolationEvaluator(mapper));
        ruleEngineService = new RuleEngineService(
                ruleRepository, transactionRepository, auditService, mapper, evaluators);
    }

    private Transaction txn() {
        return Transaction.builder()
                .transactionId("T1").customerId("C1")
                .amount(new BigDecimal("150000")).taxRate(new BigDecimal("0.18"))
                .transactionType(TransactionType.SALE)
                .build();
    }

    private Rule highValueRule() {
        return Rule.builder()
                .ruleName("HIGH_VALUE_TRANSACTION").ruleType(RuleType.HIGH_VALUE)
                .severity(Severity.HIGH).enabled(true)
                .configJson("{\"threshold\": 100000}")
                .build();
    }

    @Test
    void runsActiveRulesAndReturnsExceptionsAndAudits() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(highValueRule()));
        when(transactionRepository.findByCustomerId("C1")).thenReturn(List.of());

        List<ExceptionRecord> result = ruleEngineService.runRules(txn());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleName()).isEqualTo("HIGH_VALUE_TRANSACTION");
        assertThat(result.get(0).getSeverity()).isEqualTo(Severity.HIGH);
        verify(auditService).logRuleExecution(eq("T1"), eq("HIGH_VALUE_TRANSACTION"), anyBoolean());
    }

    @Test
    void noExceptionsWhenNoRulesActive() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of());
        when(transactionRepository.findByCustomerId("C1")).thenReturn(List.of());

        List<ExceptionRecord> result = ruleEngineService.runRules(txn());

        assertThat(result).isEmpty();
        verify(auditService, never()).logRuleExecution(anyString(), anyString(), anyBoolean());
    }
}
