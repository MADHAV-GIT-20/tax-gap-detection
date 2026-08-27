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
import com.taxgap.support.FakeJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-JUnit test (no Mockito): dependencies are hand-written fakes.
 */
class RuleEngineServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<RuleEvaluator> evaluators = List.of(
            new HighValueEvaluator(mapper),
            new RefundValidationEvaluator(),
            new GstSlabViolationEvaluator(mapper));

    // ---- fakes ----
    static class FakeRuleRepository extends FakeJpaRepository<Rule, Long> implements RuleRepository {
        List<Rule> enabled = List.of();
        @Override public List<Rule> findByEnabledTrue() { return enabled; }
        @Override public boolean existsByRuleName(String ruleName) { return false; }
    }

    static class FakeTransactionRepository extends FakeJpaRepository<Transaction, Long> implements TransactionRepository {
        List<Transaction> byCustomer = List.of();
        @Override public List<Transaction> findByCustomerId(String customerId) { return byCustomer; }
        @Override public List<com.taxgap.repository.projection.CustomerSummaryProjection> customerTaxSummary() {
            return List.of();
        }
    }

    /** Records rule-execution audit calls instead of writing to a DB. */
    static class RecordingAuditService extends AuditService {
        int ruleExecCount = 0;
        RecordingAuditService() { super(null, null); }
        @Override public void logIngestion(String transactionId, String rawPayload) { }
        @Override public void logTaxComputation(Transaction txn) { }
        @Override public void logRuleExecution(String transactionId, String ruleName, boolean violated) {
            ruleExecCount++;
        }
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
        FakeRuleRepository rules = new FakeRuleRepository();
        rules.enabled = List.of(highValueRule());
        FakeTransactionRepository txns = new FakeTransactionRepository();
        RecordingAuditService audit = new RecordingAuditService();

        RuleEngineService engine = new RuleEngineService(rules, txns, audit, mapper, evaluators);

        List<ExceptionRecord> result = engine.runRules(txn());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleName()).isEqualTo("HIGH_VALUE_TRANSACTION");
        assertThat(result.get(0).getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(audit.ruleExecCount).isEqualTo(1);
    }

    @Test
    void noExceptionsWhenNoRulesActive() {
        FakeRuleRepository rules = new FakeRuleRepository();       // enabled = empty
        FakeTransactionRepository txns = new FakeTransactionRepository();
        RecordingAuditService audit = new RecordingAuditService();

        RuleEngineService engine = new RuleEngineService(rules, txns, audit, mapper, evaluators);

        List<ExceptionRecord> result = engine.runRules(txn());

        assertThat(result).isEmpty();
        assertThat(audit.ruleExecCount).isZero();
    }
}
