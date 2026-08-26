package com.taxgap.service;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.repository.TransactionRepository;
import com.taxgap.repository.projection.CustomerSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository transactionRepository;
    private final ExceptionService exceptionService;

    /**
     * Customer tax summary. Totals are aggregated in SQL; only the compliance
     * score (a ratio) is finalised here.
     * complianceScore = 100 - (nonCompliant / total * 100).
     */
    @Transactional(readOnly = true)
    public List<CustomerSummaryDto> customerTaxSummary() {
        return transactionRepository.customerTaxSummary().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExceptionSummaryDto exceptionSummary() {
        return exceptionService.summary();
    }

    private CustomerSummaryDto toDto(CustomerSummaryProjection p) {
        BigDecimal complianceScore;
        if (p.getTotalTransactions() == 0) {
            complianceScore = HUNDRED;
        } else {
            BigDecimal nonCompliantPct = BigDecimal.valueOf(p.getNonCompliantTransactions())
                    .divide(BigDecimal.valueOf(p.getTotalTransactions()), 6, RoundingMode.HALF_UP)
                    .multiply(HUNDRED);
            complianceScore = HUNDRED.subtract(nonCompliantPct).setScale(2, RoundingMode.HALF_UP);
        }
        return new CustomerSummaryDto(
                p.getCustomerId(),
                p.getTotalAmount(),
                p.getTotalReportedTax(),
                p.getTotalExpectedTax(),
                p.getTotalTaxGap(),
                p.getTotalTransactions(),
                p.getNonCompliantTransactions(),
                complianceScore
        );
    }
}
