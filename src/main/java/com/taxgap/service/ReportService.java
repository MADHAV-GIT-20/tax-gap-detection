package com.taxgap.service;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TransactionRepository transactionRepository;
    private final ExceptionService exceptionService;

    public ReportService(TransactionRepository transactionRepository, ExceptionService exceptionService) {
        this.transactionRepository = transactionRepository;
        this.exceptionService = exceptionService;
    }

    /**
     * Customer tax summary. The totals come from the database query; here we only
     * finish the compliance score = 100 - (nonCompliant / total * 100).
     */
    public List<CustomerSummaryDto> customerTaxSummary() {
        List<CustomerSummaryDto> rows = transactionRepository.customerTaxSummary();
        for (CustomerSummaryDto row : rows) {
            row.setComplianceScore(complianceScore(row.getTotalTransactions(), row.getNonCompliantTransactions()));
        }
        return rows;
    }

    public ExceptionSummaryDto exceptionSummary() {
        return exceptionService.summary();
    }

    private BigDecimal complianceScore(long total, long nonCompliant) {
        if (total == 0) {
            return HUNDRED.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal nonCompliantPct = BigDecimal.valueOf(nonCompliant)
                .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED);
        return HUNDRED.subtract(nonCompliantPct).setScale(2, RoundingMode.HALF_UP);
    }
}
