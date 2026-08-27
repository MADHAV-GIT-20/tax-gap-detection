package com.taxgap.repository;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerId(String customerId);

    /**
     * Customer tax summary, aggregated by the database (not in Java memory).
     * Only SUCCESS transactions are counted.
     */
    @Query("""
            SELECT new com.taxgap.dto.CustomerSummaryDto(
                       t.customerId,
                       SUM(t.amount),
                       SUM(t.reportedTax),
                       SUM(t.expectedTax),
                       SUM(t.taxGap),
                       COUNT(t),
                       SUM(CASE WHEN t.complianceStatus = com.taxgap.enums.ComplianceStatus.NON_COMPLIANT
                                THEN 1L ELSE 0L END))
            FROM Transaction t
            WHERE t.validationStatus = com.taxgap.enums.ValidationStatus.SUCCESS
            GROUP BY t.customerId
            ORDER BY t.customerId
            """)
    List<CustomerSummaryDto> customerTaxSummary();
}
