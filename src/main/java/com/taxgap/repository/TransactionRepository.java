package com.taxgap.repository;

import com.taxgap.domain.Transaction;
import com.taxgap.repository.projection.CustomerSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerId(String customerId);

    /**
     * Customer tax summary report, aggregated in SQL (not in memory).
     * complianceScore = 100 - (nonCompliant / total * 100).
     */
    @Query("""
            SELECT t.customerId AS customerId,
                   COALESCE(SUM(t.amount), 0) AS totalAmount,
                   COALESCE(SUM(t.reportedTax), 0) AS totalReportedTax,
                   COALESCE(SUM(t.expectedTax), 0) AS totalExpectedTax,
                   COALESCE(SUM(t.taxGap), 0) AS totalTaxGap,
                   COUNT(t) AS totalTransactions,
                   SUM(CASE WHEN t.complianceStatus = com.taxgap.domain.enums.ComplianceStatus.NON_COMPLIANT
                            THEN 1 ELSE 0 END) AS nonCompliantTransactions
            FROM Transaction t
            WHERE t.validationStatus = com.taxgap.domain.enums.ValidationStatus.SUCCESS
            GROUP BY t.customerId
            ORDER BY t.customerId
            """)
    List<CustomerSummaryProjection> customerTaxSummary();
}
