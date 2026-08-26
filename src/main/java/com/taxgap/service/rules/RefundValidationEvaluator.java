package com.taxgap.service.rules;

import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.RuleType;
import com.taxgap.domain.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * (B) Refund Validation Rule.
 * A refund amount must not exceed the customer's total SALE amount.
 * No JSON config required.
 */
@Component
public class RefundValidationEvaluator implements RuleEvaluator {

    @Override
    public RuleType supportedType() {
        return RuleType.REFUND_VALIDATION;
    }

    @Override
    public Optional<String> evaluate(Transaction txn, Rule rule, RuleEvaluationContext context) {
        if (txn.getTransactionType() != TransactionType.REFUND || txn.getAmount() == null) {
            return Optional.empty();
        }
        BigDecimal totalSales = context.customerTotalSales() == null
                ? BigDecimal.ZERO : context.customerTotalSales();

        if (txn.getAmount().compareTo(totalSales) > 0) {
            return Optional.of("Refund amount " + txn.getAmount()
                    + " exceeds customer's total sales " + totalSales);
        }
        return Optional.empty();
    }
}
