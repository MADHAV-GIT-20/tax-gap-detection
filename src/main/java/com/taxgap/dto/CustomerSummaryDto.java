package com.taxgap.dto;

import java.math.BigDecimal;

public record CustomerSummaryDto(
        String customerId,
        BigDecimal totalAmount,
        BigDecimal totalReportedTax,
        BigDecimal totalExpectedTax,
        BigDecimal totalTaxGap,
        long totalTransactions,
        long nonCompliantTransactions,
        BigDecimal complianceScore
) {
}
