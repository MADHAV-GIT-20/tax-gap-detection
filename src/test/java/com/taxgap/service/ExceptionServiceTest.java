package com.taxgap.service;

import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.repository.ExceptionRecordRepository;
import com.taxgap.repository.projection.CountByKeyProjection;
import com.taxgap.support.FakeJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-JUnit test (no Mockito): the repository is a hand-written fake.
 */
class ExceptionServiceTest {

    static class FakeExceptionRecordRepository extends FakeJpaRepository<ExceptionRecord, Long>
            implements ExceptionRecordRepository {

        long total = 0;
        Map<Severity, Long> bySeverity = Map.of();
        List<CountByKeyProjection> byCustomer = List.of();
        // captured search arguments
        String lastCustomerId = "unset";
        Severity lastSeverity;
        String lastRuleName = "unset";

        @Override public long count() { return total; }

        @Override public List<ExceptionRecord> search(String customerId, Severity severity, String ruleName) {
            this.lastCustomerId = customerId;
            this.lastSeverity = severity;
            this.lastRuleName = ruleName;
            return List.of();
        }

        @Override public long countBySeverity(Severity severity) {
            return bySeverity.getOrDefault(severity, 0L);
        }

        @Override public List<CountByKeyProjection> countByCustomer() { return byCustomer; }
    }

    private CountByKeyProjection count(String key, long c) {
        return new CountByKeyProjection() {
            public String getKey() { return key; }
            public long getCount() { return c; }
        };
    }

    @Test
    void summaryAggregatesTotalsSeverityAndCustomer() {
        FakeExceptionRecordRepository repo = new FakeExceptionRecordRepository();
        repo.total = 5;
        repo.bySeverity = Map.of(Severity.HIGH, 3L, Severity.MEDIUM, 2L, Severity.LOW, 0L);
        repo.byCustomer = List.of(count("C1", 4), count("C2", 1));

        ExceptionService service = new ExceptionService(repo);
        ExceptionSummaryDto summary = service.summary();

        assertThat(summary.totalExceptions()).isEqualTo(5);
        assertThat(summary.countBySeverity()).containsEntry("HIGH", 3L).containsEntry("MEDIUM", 2L);
        assertThat(summary.countByCustomer()).containsEntry("C1", 4L).containsEntry("C2", 1L);
    }

    @Test
    void searchConvertsBlankFiltersToNull() {
        FakeExceptionRecordRepository repo = new FakeExceptionRecordRepository();
        ExceptionService service = new ExceptionService(repo);

        assertThat(service.search("", null, "  ")).isEmpty();
        // blanks were normalised to null before hitting the repository
        assertThat(repo.lastCustomerId).isNull();
        assertThat(repo.lastRuleName).isNull();
    }
}
