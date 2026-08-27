package com.taxgap.service;

import com.taxgap.entity.ExceptionRecord;
import com.taxgap.entity.Rule;
import com.taxgap.entity.Transaction;
import com.taxgap.enums.RuleType;
import com.taxgap.enums.Severity;
import com.taxgap.enums.TransactionType;
import com.taxgap.support.FakeAuditLogRepository;
import com.taxgap.support.FakeRuleRepository;
import com.taxgap.support.FakeTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleServiceTest {

    private FakeRuleRepository ruleRepo;
    private FakeTransactionRepository txnRepo;
    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        ruleRepo = new FakeRuleRepository();
        txnRepo = new FakeTransactionRepository();
        ruleService = new RuleService(ruleRepo, txnRepo, new AuditService(new FakeAuditLogRepository()));
    }

    private Rule rule(String name, RuleType type, String configJson) {
        Rule r = new Rule();
        r.setRuleName(name);
        r.setRuleType(type);
        r.setSeverity(Severity.HIGH);
        r.setEnabled(true);
        r.setConfigJson(configJson);
        return ruleRepo.add(r);
    }

    private Transaction txn(String amount, String rate, TransactionType type) {
        Transaction t = new Transaction();
        t.setTransactionId("T1");
        t.setCustomerId("C1");
        t.setAmount(new BigDecimal(amount));
        t.setTaxRate(new BigDecimal(rate));
        t.setTransactionType(type);
        return t;
    }

    @Test
    void noRulesMeansNoExceptions() {
        List<ExceptionRecord> result = ruleService.runRules(txn("1000", "0.18", TransactionType.SALE));
        assertThat(result).isEmpty();
    }

    @Test
    void highValueRuleRaisesException() {
        rule("HIGH_VALUE_TRANSACTION", RuleType.HIGH_VALUE, "{\"threshold\": 100000}");
        List<ExceptionRecord> result = ruleService.runRules(txn("150000", "0.18", TransactionType.SALE));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleName()).isEqualTo("HIGH_VALUE_TRANSACTION");
        assertThat(result.get(0).getSeverity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void highValueRulePassesUnderThreshold() {
        rule("HIGH_VALUE_TRANSACTION", RuleType.HIGH_VALUE, "{\"threshold\": 100000}");
        List<ExceptionRecord> result = ruleService.runRules(txn("5000", "0.18", TransactionType.SALE));
        assertThat(result).isEmpty();
    }

    @Test
    void refundExceedingSalesRaisesException() {
        rule("REFUND_EXCEEDS_SALES", RuleType.REFUND_VALIDATION, "{}");
        // customer C1 has 3000 in sales
        Transaction sale = txn("3000", "0.18", TransactionType.SALE);
        txnRepo.save(sale);
        List<ExceptionRecord> result = ruleService.runRules(txn("5000", "0", TransactionType.REFUND));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleName()).isEqualTo("REFUND_EXCEEDS_SALES");
    }

    @Test
    void gstSlabViolationRaisesException() {
        rule("GST_SLAB_VIOLATION", RuleType.GST_SLAB_VIOLATION,
                "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}");
        List<ExceptionRecord> result = ruleService.runRules(txn("60000", "0.12", TransactionType.SALE));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRuleName()).isEqualTo("GST_SLAB_VIOLATION");
    }

    @Test
    void gstSlabPassesWhenRateMeetsRequirement() {
        rule("GST_SLAB_VIOLATION", RuleType.GST_SLAB_VIOLATION,
                "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}");
        List<ExceptionRecord> result = ruleService.runRules(txn("60000", "0.18", TransactionType.SALE));
        assertThat(result).isEmpty();
    }
}
