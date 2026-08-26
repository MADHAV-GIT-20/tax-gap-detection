package com.taxgap.repository;

import com.taxgap.domain.ExceptionRecord;
import com.taxgap.domain.enums.Severity;
import com.taxgap.repository.projection.CountByKeyProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, Long> {

    /**
     * Dynamic filtering: any of the three params may be null (ignored).
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

    @Query("""
            SELECT e.customerId AS key, COUNT(e) AS count
            FROM ExceptionRecord e
            GROUP BY e.customerId
            ORDER BY e.customerId
            """)
    List<CountByKeyProjection> countByCustomer();
}
