package com.taxgap.dto;

import java.math.BigDecimal;

/**
 * One row of the customer tax summary report.
 *
 * The constructor is used directly by a JPQL query (SELECT new ...), which fills
 * in the totals. The complianceScore is calculated afterwards in ReportService.
 */
public class CustomerSummaryDto {

    private String customerId;
    private BigDecimal totalAmount;
    private BigDecimal totalReportedTax;
    private BigDecimal totalExpectedTax;
    private BigDecimal totalTaxGap;
    private long totalTransactions;
    private long nonCompliantTransactions;
    private BigDecimal complianceScore;

    public CustomerSummaryDto(String customerId,
                              BigDecimal totalAmount,
                              BigDecimal totalReportedTax,
                              BigDecimal totalExpectedTax,
                              BigDecimal totalTaxGap,
                              long totalTransactions,
                              long nonCompliantTransactions) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.totalReportedTax = totalReportedTax;
        this.totalExpectedTax = totalExpectedTax;
        this.totalTaxGap = totalTaxGap;
        this.totalTransactions = totalTransactions;
        this.nonCompliantTransactions = nonCompliantTransactions;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getTotalReportedTax() {
        return totalReportedTax;
    }

    public BigDecimal getTotalExpectedTax() {
        return totalExpectedTax;
    }

    public BigDecimal getTotalTaxGap() {
        return totalTaxGap;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public long getNonCompliantTransactions() {
        return nonCompliantTransactions;
    }

    public BigDecimal getComplianceScore() {
        return complianceScore;
    }

    public void setComplianceScore(BigDecimal complianceScore) {
        this.complianceScore = complianceScore;
    }
}
