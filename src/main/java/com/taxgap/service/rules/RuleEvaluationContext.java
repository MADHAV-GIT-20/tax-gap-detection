package com.taxgap.service.rules;

import java.math.BigDecimal;

/**
 * Extra data an evaluator may need beyond the transaction itself.
 *
 * @param customerTotalSales sum of the customer's SALE transaction amounts
 *                           (used by the refund-validation rule)
 */
public record RuleEvaluationContext(BigDecimal customerTotalSales) {
}
