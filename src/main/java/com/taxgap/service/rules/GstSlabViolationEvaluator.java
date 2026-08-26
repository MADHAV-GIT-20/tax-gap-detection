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
 * (C) GST Slab Violation Rule.
 * config: {"slabThreshold": 50000, "requiredRate": 0.18}
 * If amount exceeds the slab threshold but taxRate is lower than the required
 * slab rate, flag an exception.
 */
@Component
@RequiredArgsConstructor
public class GstSlabViolationEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.GST_SLAB_VIOLATION;
    }

    @Override
    public Optional<String> evaluate(Transaction txn, Rule rule, RuleEvaluationContext context) {
        if (txn.getAmount() == null || txn.getTaxRate() == null) {
            return Optional.empty();
        }
        BigDecimal slabThreshold = RuleConfigReader.readBigDecimal(objectMapper, rule.getConfigJson(), "slabThreshold");
        BigDecimal requiredRate = RuleConfigReader.readBigDecimal(objectMapper, rule.getConfigJson(), "requiredRate");
        if (slabThreshold == null || requiredRate == null) {
            return Optional.empty();
        }
        boolean overSlab = txn.getAmount().compareTo(slabThreshold) > 0;
        boolean rateTooLow = txn.getTaxRate().compareTo(requiredRate) < 0;
        if (overSlab && rateTooLow) {
            return Optional.of("GST slab violation: amount " + txn.getAmount()
                    + " exceeds slab " + slabThreshold + " but taxRate " + txn.getTaxRate()
                    + " is below required " + requiredRate);
        }
        return Optional.empty();
    }
}
