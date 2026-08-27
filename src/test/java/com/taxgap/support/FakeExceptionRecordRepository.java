package com.taxgap.support;

import com.taxgap.entity.ExceptionRecord;
import com.taxgap.enums.Severity;
import com.taxgap.repository.ExceptionRecordRepository;

import java.util.ArrayList;
import java.util.List;

/** Simple in-memory fake of ExceptionRecordRepository for tests. */
public class FakeExceptionRecordRepository extends FakeJpaRepository<ExceptionRecord, Long>
        implements ExceptionRecordRepository {

    public final List<ExceptionRecord> store = new ArrayList<>();

    @Override
    public <S extends ExceptionRecord> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S e : entities) {
            store.add(e);
            saved.add(e);
        }
        return saved;
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public long countBySeverity(Severity severity) {
        return store.stream().filter(e -> e.getSeverity() == severity).count();
    }

    @Override
    public List<Object[]> countByCustomer() {
        return new ArrayList<>();
    }

    @Override
    public List<ExceptionRecord> search(String customerId, Severity severity, String ruleName) {
        List<ExceptionRecord> result = new ArrayList<>();
        for (ExceptionRecord e : store) {
            if (customerId != null && !customerId.equals(e.getCustomerId())) {
                continue;
            }
            if (severity != null && severity != e.getSeverity()) {
                continue;
            }
            if (ruleName != null && !ruleName.equals(e.getRuleName())) {
                continue;
            }
            result.add(e);
        }
        return result;
    }
}
