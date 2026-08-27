package com.taxgap.service;

import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.entity.ExceptionRecord;
import com.taxgap.enums.Severity;
import com.taxgap.support.FakeExceptionRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionServiceTest {

    private ExceptionRecord record(String customerId, Severity severity, String ruleName) {
        ExceptionRecord e = new ExceptionRecord();
        e.setTransactionId("T1");
        e.setCustomerId(customerId);
        e.setSeverity(severity);
        e.setRuleName(ruleName);
        e.setMessage("msg");
        return e;
    }

    @Test
    void summaryCountsTotalAndBySeverity() {
        FakeExceptionRecordRepository repo = new FakeExceptionRecordRepository();
        ExceptionService service = new ExceptionService(repo);
        service.saveAll(List.of(
                record("C1", Severity.HIGH, "R1"),
                record("C1", Severity.HIGH, "R2"),
                record("C2", Severity.MEDIUM, "R1")));

        ExceptionSummaryDto summary = service.summary();

        assertThat(summary.getTotalExceptions()).isEqualTo(3);
        assertThat(summary.getCountBySeverity()).containsEntry("HIGH", 2L).containsEntry("MEDIUM", 1L);
    }

    @Test
    void searchFiltersBySeverity() {
        FakeExceptionRecordRepository repo = new FakeExceptionRecordRepository();
        ExceptionService service = new ExceptionService(repo);
        service.saveAll(List.of(
                record("C1", Severity.HIGH, "R1"),
                record("C2", Severity.LOW, "R2")));

        List<ExceptionRecord> highOnly = service.search(null, Severity.HIGH, null);

        assertThat(highOnly).hasSize(1);
        assertThat(highOnly.get(0).getCustomerId()).isEqualTo("C1");
    }

    @Test
    void searchTreatsBlankFiltersAsNull() {
        FakeExceptionRecordRepository repo = new FakeExceptionRecordRepository();
        ExceptionService service = new ExceptionService(repo);
        service.saveAll(List.of(record("C1", Severity.HIGH, "R1")));

        // blank customerId / ruleName should not filter anything out
        assertThat(service.search("  ", null, "")).hasSize(1);
    }
}
