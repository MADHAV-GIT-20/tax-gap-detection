package com.taxgap.controller;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Customer tax summary report. */
    @GetMapping("/customer-summary")
    public List<CustomerSummaryDto> customerSummary() {
        return reportService.customerTaxSummary();
    }

    /** Exception summary report. */
    @GetMapping("/exception-summary")
    public ExceptionSummaryDto exceptionSummary() {
        return reportService.exceptionSummary();
    }
}
