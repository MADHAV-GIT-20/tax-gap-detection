package com.taxgap.service;

import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.ExceptionSummaryDto;
import com.taxgap.repository.ExceptionRecordRepository;
import com.taxgap.repository.projection.CountByKeyProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionServiceTest {

    @Mock
    private ExceptionRecordRepository repository;
    @InjectMocks
    private ExceptionService exceptionService;

    private CountByKeyProjection count(String key, long c) {
        return new CountByKeyProjection() {
            public String getKey() { return key; }
            public long getCount() { return c; }
        };
    }

    @Test
    void summaryAggregatesTotalsSeverityAndCustomer() {
        when(repository.count()).thenReturn(5L);
        when(repository.countBySeverity(Severity.HIGH)).thenReturn(3L);
        when(repository.countBySeverity(Severity.MEDIUM)).thenReturn(2L);
        when(repository.countBySeverity(Severity.LOW)).thenReturn(0L);
        when(repository.countByCustomer()).thenReturn(List.of(count("C1", 4), count("C2", 1)));

        ExceptionSummaryDto summary = exceptionService.summary();

        assertThat(summary.totalExceptions()).isEqualTo(5);
        assertThat(summary.countBySeverity()).containsEntry("HIGH", 3L).containsEntry("MEDIUM", 2L);
        assertThat(summary.countByCustomer()).containsEntry("C1", 4L).containsEntry("C2", 1L);
    }

    @Test
    void searchConvertsBlankFiltersToNull() {
        when(repository.search(null, null, null)).thenReturn(List.of());
        assertThat(exceptionService.search("", null, "  ")).isEmpty();
    }
}
