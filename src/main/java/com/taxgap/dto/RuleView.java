package com.taxgap.dto;

import com.taxgap.domain.Rule;

public record RuleView(
        Long id,
        String ruleName,
        String ruleType,
        String severity,
        boolean enabled,
        String configJson,
        String description
) {
    public static RuleView from(Rule r) {
        return new RuleView(
                r.getId(),
                r.getRuleName(),
                r.getRuleType() == null ? null : r.getRuleType().name(),
                r.getSeverity() == null ? null : r.getSeverity().name(),
                r.isEnabled(),
                r.getConfigJson(),
                r.getDescription()
        );
    }
}
