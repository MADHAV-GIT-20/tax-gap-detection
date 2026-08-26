package com.taxgap.service.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.RuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * (A) High-Value Transaction Rule.
 * config: {"threshold": 100000}
 */
@Component
@RequiredArgsConstructor
public class HighValueEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.HIGH_VALUE;
    }

    @Override
    public Optional<String> evaluate(Transaction txn, Rule rule, RuleEvaluationContext context) {
        if (txn.getAmount() == null) {
            return Optional.empty();
        }
        BigDecimal threshold = RuleConfigReader.readBigDecimal(objectMapper, rule.getConfigJson(), "threshold");
        if (threshold == null) {
            return Optional.empty();
        }
        if (txn.getAmount().compareTo(threshold) > 0) {
            return Optional.of("High-value transaction: amount " + txn.getAmount()
                    + " exceeds threshold " + threshold);
        }
        return Optional.empty();
    }
}
