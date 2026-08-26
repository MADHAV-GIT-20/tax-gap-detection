package com.taxgap.dto;

import java.util.Map;

public record ExceptionSummaryDto(
        long totalExceptions,
        Map<String, Long> countBySeverity,
        Map<String, Long> countByCustomer
) {
}
