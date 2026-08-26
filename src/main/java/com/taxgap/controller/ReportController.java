package com.taxgap.controller;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** A. Customer Tax Summary Report. */
    @GetMapping("/customer-summary")
    public List<CustomerSummaryDto> customerSummary() {
        return reportService.customerTaxSummary();
    }

    /** B. Exception Summary Report. */
    @GetMapping("/exception-summary")
    public ExceptionSummaryDto exceptionSummary() {
        return reportService.exceptionSummary();
    }
}
