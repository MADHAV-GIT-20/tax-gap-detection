package com.taxgap.repository.projection;

import java.math.BigDecimal;

/**
 * Spring Data projection for the customer tax summary aggregate query.
 */
public interface CustomerSummaryProjection {
    String getCustomerId();
    BigDecimal getTotalAmount();
    BigDecimal getTotalReportedTax();
    BigDecimal getTotalExpectedTax();
    BigDecimal getTotalTaxGap();
    long getTotalTransactions();
    long getNonCompliantTransactions();
}
