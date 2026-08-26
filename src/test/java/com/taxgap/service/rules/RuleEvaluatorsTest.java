package com.taxgap.service.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.Severity;
import com.taxgap.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluatorsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private Transaction txn(String amount, String rate, TransactionType type) {
        return Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .amount(new BigDecimal(amount))
                .taxRate(rate == null ? null : new BigDecimal(rate))
                .transactionType(type)
                .build();
    }

    private Rule rule(RuleType type, String config) {
        return Rule.builder()
                .ruleName(type.name())
                .ruleType(type)
                .severity(Severity.HIGH)
                .enabled(true)
                .configJson(config)
                .build();
    }

    // ---- High value ----
    @Test
    void highValueFlagsWhenOverThreshold() {
        HighValueEvaluator e = new HighValueEvaluator(mapper);
        Optional<String> r = e.evaluate(txn("150000", "0.18", TransactionType.SALE),
                rule(RuleType.HIGH_VALUE, "{\"threshold\": 100000}"), new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isPresent();
    }

    @Test
    void highValuePassesWhenUnderThreshold() {
        HighValueEvaluator e = new HighValueEvaluator(mapper);
        Optional<String> r = e.evaluate(txn("5000", "0.18", TransactionType.SALE),
                rule(RuleType.HIGH_VALUE, "{\"threshold\": 100000}"), new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isEmpty();
    }

    // ---- Refund ----
    @Test
    void refundFlagsWhenExceedingSales() {
        RefundValidationEvaluator e = new RefundValidationEvaluator();
        Optional<String> r = e.evaluate(txn("5000", "0", TransactionType.REFUND),
                rule(RuleType.REFUND_VALIDATION, "{}"), new RuleEvaluationContext(new BigDecimal("3000")));
        assertThat(r).isPresent();
    }

    @Test
    void refundPassesWhenWithinSales() {
        RefundValidationEvaluator e = new RefundValidationEvaluator();
        Optional<String> r = e.evaluate(txn("2000", "0", TransactionType.REFUND),
                rule(RuleType.REFUND_VALIDATION, "{}"), new RuleEvaluationContext(new BigDecimal("3000")));
        assertThat(r).isEmpty();
    }

    @Test
    void refundIgnoredForNonRefundType() {
        RefundValidationEvaluator e = new RefundValidationEvaluator();
        Optional<String> r = e.evaluate(txn("5000", "0", TransactionType.SALE),
                rule(RuleType.REFUND_VALIDATION, "{}"), new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isEmpty();
    }

    // ---- GST slab ----
    @Test
    void gstSlabFlagsWhenOverSlabAndRateTooLow() {
        GstSlabViolationEvaluator e = new GstSlabViolationEvaluator(mapper);
        Optional<String> r = e.evaluate(txn("60000", "0.12", TransactionType.SALE),
                rule(RuleType.GST_SLAB_VIOLATION, "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}"),
                new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isPresent();
    }

    @Test
    void gstSlabPassesWhenRateMeetsRequirement() {
        GstSlabViolationEvaluator e = new GstSlabViolationEvaluator(mapper);
        Optional<String> r = e.evaluate(txn("60000", "0.18", TransactionType.SALE),
                rule(RuleType.GST_SLAB_VIOLATION, "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}"),
                new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isEmpty();
    }

    @Test
    void gstSlabPassesWhenUnderSlab() {
        GstSlabViolationEvaluator e = new GstSlabViolationEvaluator(mapper);
        Optional<String> r = e.evaluate(txn("40000", "0.05", TransactionType.SALE),
                rule(RuleType.GST_SLAB_VIOLATION, "{\"slabThreshold\": 50000, \"requiredRate\": 0.18}"),
                new RuleEvaluationContext(BigDecimal.ZERO));
        assertThat(r).isEmpty();
    }
}
