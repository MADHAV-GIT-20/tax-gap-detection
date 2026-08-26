package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.domain.enums.ComplianceStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes expected tax, the tax gap and the compliance status for a valid transaction.
 *
 * expectedTax = amount * taxRate
 * taxGap      = expectedTax - reportedTax
 *
 * Compliance:
 *   |taxGap| <= 1  -> COMPLIANT
 *   taxGap  >  1   -> UNDERPAID
 *   taxGap  < -1   -> OVERPAID
 *   reportedTax missing -> NON_COMPLIANT
 */
@Service
public class TaxGapService {

    /** Tolerance band for rounding differences. */
    private static final BigDecimal TOLERANCE = BigDecimal.ONE;
    private static final int SCALE = 4;

    /**
     * Mutates the transaction in place with expectedTax, taxGap and complianceStatus.
     */
    public void compute(Transaction txn) {
        BigDecimal expectedTax = txn.getAmount()
                .multiply(txn.getTaxRate())
                .setScale(SCALE, RoundingMode.HALF_UP);
        txn.setExpectedTax(expectedTax);

        if (txn.getReportedTax() == null) {
            txn.setTaxGap(null);
            txn.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
            return;
        }

        BigDecimal taxGap = expectedTax.subtract(txn.getReportedTax())
                .setScale(SCALE, RoundingMode.HALF_UP);
        txn.setTaxGap(taxGap);
        txn.setComplianceStatus(classify(taxGap));
    }

    private ComplianceStatus classify(BigDecimal taxGap) {
        if (taxGap.abs().compareTo(TOLERANCE) <= 0) {
            return ComplianceStatus.COMPLIANT;
        }
        if (taxGap.compareTo(TOLERANCE) > 0) {
            return ComplianceStatus.UNDERPAID;
        }
        return ComplianceStatus.OVERPAID;
    }
}
