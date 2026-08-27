package com.taxgap.service;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.support.FakeExceptionRecordRepository;
import com.taxgap.support.FakeTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportServiceTest {

    private CustomerSummaryDto row(String cust, long total, long nonCompliant) {
        return new CustomerSummaryDto(cust,
                new BigDecimal("1000"), new BigDecimal("100"),
                new BigDecimal("180"), new BigDecimal("80"),
                total, nonCompliant);
    }

    private ReportService serviceWith(List<CustomerSummaryDto> rows) {
        FakeTransactionRepository txnRepo = new FakeTransactionRepository();
        txnRepo.summaryRows = rows;
        return new ReportService(txnRepo, new ExceptionService(new FakeExceptionRecordRepository()));
    }

    @Test
    void complianceScoreIs100WhenAllCompliant() {
        ReportService service = serviceWith(List.of(row("C1", 4, 0)));
        List<CustomerSummaryDto> result = service.customerTaxSummary();
        assertThat(result.get(0).getComplianceScore()).isEqualByComparingTo("100.00");
    }

    @Test
    void complianceScoreReflectsNonCompliantRatio() {
        // 1 of 4 non-compliant -> 100 - 25 = 75
        ReportService service = serviceWith(List.of(row("C1", 4, 1)));
        List<CustomerSummaryDto> result = service.customerTaxSummary();
        assertThat(result.get(0).getComplianceScore()).isEqualByComparingTo("75.00");
    }
}
