package com.taxgap.dto;

import java.util.Map;

/** The exception summary report. */
public class ExceptionSummaryDto {

    private long totalExceptions;
    private Map<String, Long> countBySeverity;
    private Map<String, Long> countByCustomer;

    public long getTotalExceptions() {
        return totalExceptions;
    }

    public void setTotalExceptions(long totalExceptions) {
        this.totalExceptions = totalExceptions;
    }

    public Map<String, Long> getCountBySeverity() {
        return countBySeverity;
    }

    public void setCountBySeverity(Map<String, Long> countBySeverity) {
        this.countBySeverity = countBySeverity;
    }

    public Map<String, Long> getCountByCustomer() {
        return countByCustomer;
    }

    public void setCountByCustomer(Map<String, Long> countByCustomer) {
        this.countByCustomer = countByCustomer;
    }
}
