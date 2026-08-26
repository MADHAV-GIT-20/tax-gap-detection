package com.taxgap.service.rules;

import com.taxgap.domain.Rule;
import com.taxgap.domain.Transaction;

import java.util.Optional;

/**
 * A single compliance rule strategy. One implementation per {@link com.taxgap.domain.enums.RuleType}.
 */
public interface RuleEvaluator {

    com.taxgap.domain.enums.RuleType supportedType();

    /**
     * @return a violation message if the rule is breached, otherwise empty.
     */
    Optional<String> evaluate(Transaction txn, Rule rule, RuleEvaluationContext context);
}
