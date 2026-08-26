package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.ComplianceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxGapServiceTest {

    private final TaxGapService service = new TaxGapService();

    private Transaction txn(String amount, String taxRate, String reportedTax) {
        return Transaction.builder()
                .amount(new BigDecimal(amount))
                .taxRate(new BigDecimal(taxRate))
                .reportedTax(reportedTax == null ? null : new BigDecimal(reportedTax))
                .build();
    }

    @Test
    void computesExpectedTaxAsAmountTimesRate() {
        Transaction t = txn("1000", "0.18", "180");
        service.compute(t);
        assertThat(t.getExpectedTax()).isEqualByComparingTo("180.0000");
    }

    @Test
    void compliantWhenGapWithinTolerance() {
        Transaction t = txn("1000", "0.18", "179.5"); // gap 0.5 -> within +/-1
        service.compute(t);
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void underpaidWhenGapAboveOne() {
        Transaction t = txn("1000", "0.18", "150"); // expected 180, gap +30
        service.compute(t);
        assertThat(t.getTaxGap()).isEqualByComparingTo("30.0000");
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.UNDERPAID);
    }

    @Test
    void overpaidWhenGapBelowMinusOne() {
        Transaction t = txn("1000", "0.18", "200"); // expected 180, gap -20
        service.compute(t);
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.OVERPAID);
    }

    @Test
    void nonCompliantWhenReportedTaxMissing() {
        Transaction t = txn("1000", "0.18", null);
        service.compute(t);
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.NON_COMPLIANT);
        assertThat(t.getTaxGap()).isNull();
        assertThat(t.getExpectedTax()).isEqualByComparingTo("180.0000");
    }

    @Test
    void boundaryExactlyOneIsCompliant() {
        Transaction t = txn("1000", "0.18", "179"); // gap exactly +1
        service.compute(t);
        assertThat(t.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }
}
