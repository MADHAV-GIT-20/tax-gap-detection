package com.taxgap.repository;

import com.taxgap.entity.ExceptionRecord;
import com.taxgap.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, Long> {

    /**
     * Search with optional filters. Any argument that is null is ignored.
     */
    @Query("""
            SELECT e FROM ExceptionRecord e
            WHERE (:customerId IS NULL OR e.customerId = :customerId)
              AND (:severity IS NULL OR e.severity = :severity)
              AND (:ruleName IS NULL OR e.ruleName = :ruleName)
            ORDER BY e.timestamp DESC
            """)
    List<ExceptionRecord> search(@Param("customerId") String customerId,
                                 @Param("severity") Severity severity,
                                 @Param("ruleName") String ruleName);

    long countBySeverity(Severity severity);

    /** Returns rows of [customerId, count] for the exception summary report. */
    @Query("SELECT e.customerId, COUNT(e) FROM ExceptionRecord e GROUP BY e.customerId ORDER BY e.customerId")
    List<Object[]> countByCustomer();
}
