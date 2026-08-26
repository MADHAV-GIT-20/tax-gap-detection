package com.taxgap.domain.enums;

/**
 * Identifies which evaluator strategy handles a given DB rule row.
 */
public enum RuleType {
    HIGH_VALUE,
    REFUND_VALIDATION,
    GST_SLAB_VIOLATION
}
