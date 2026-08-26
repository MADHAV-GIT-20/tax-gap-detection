package com.taxgap.service;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.repository.TransactionRepository;
import com.taxgap.repository.projection.CustomerSummaryProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ExceptionService exceptionService;
    @InjectMocks
    private ReportService reportService;

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

    @Test
    void complianceScoreIs100WhenAllCompliant() {
        when(transactionRepository.customerTaxSummary())
                .thenReturn(List.of(projection("C1", 4, 0)));
        List<CustomerSummaryDto> result = reportService.customerTaxSummary();
        assertThat(result.get(0).complianceScore()).isEqualByComparingTo("100.00");
    }

    @Test
    void complianceScoreReflectsNonCompliantRatio() {
        // 1 of 4 non-compliant -> 100 - 25 = 75
        when(transactionRepository.customerTaxSummary())
                .thenReturn(List.of(projection("C1", 4, 1)));
        List<CustomerSummaryDto> result = reportService.customerTaxSummary();
        assertThat(result.get(0).complianceScore()).isEqualByComparingTo("75.00");
        assertThat(result.get(0).totalTaxGap()).isEqualByComparingTo("80");
    }
}
