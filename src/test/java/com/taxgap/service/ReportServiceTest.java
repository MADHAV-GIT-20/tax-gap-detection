package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.repository.TransactionRepository;
import com.taxgap.repository.projection.CustomerSummaryProjection;
import com.taxgap.support.FakeJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-JUnit test (no Mockito): the repository is a hand-written fake.
 */
class ReportServiceTest {

    static class FakeTransactionRepository extends FakeJpaRepository<Transaction, Long> implements TransactionRepository {
        List<CustomerSummaryProjection> summary = List.of();
        @Override public List<Transaction> findByCustomerId(String customerId) { return List.of(); }
        @Override public List<CustomerSummaryProjection> customerTaxSummary() { return summary; }
    }

    private CustomerSummaryProjection projection(String cust, long total, long nonCompliant) {
        return new CustomerSummaryProjection() {
            public String getCustomerId() { return cust; }
            public BigDecimal getTotalAmount() { return new BigDecimal("1000"); }
            public BigDecimal getTotalReportedTax() { return new BigDecimal("100"); }
            public BigDecimal getTotalExpectedTax() { return new BigDecimal("180"); }
            public BigDecimal getTotalTaxGap() { return new BigDecimal("80"); }
            public long getTotalTransactions() { return total; }
            public long getNonCompliantTransactions() { return nonCompliant; }
        };
    }

    private ReportService newService(List<CustomerSummaryProjection> summary) {
        FakeTransactionRepository repo = new FakeTransactionRepository();
        repo.summary = summary;
        // exceptionService is unused by customerTaxSummary()
        return new ReportService(repo, null);
    }

    @Test
    void complianceScoreIs100WhenAllCompliant() {
        ReportService service = newService(List.of(projection("C1", 4, 0)));
        List<CustomerSummaryDto> result = service.customerTaxSummary();
        assertThat(result.get(0).complianceScore()).isEqualByComparingTo("100.00");
    }

    @Test
    void complianceScoreReflectsNonCompliantRatio() {
        // 1 of 4 non-compliant -> 100 - 25 = 75
        ReportService service = newService(List.of(projection("C1", 4, 1)));
        List<CustomerSummaryDto> result = service.customerTaxSummary();
        assertThat(result.get(0).complianceScore()).isEqualByComparingTo("75.00");
        assertThat(result.get(0).totalTaxGap()).isEqualByComparingTo("80");
    }
}
