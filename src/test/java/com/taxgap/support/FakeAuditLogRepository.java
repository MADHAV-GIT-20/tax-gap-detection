package com.taxgap.support;

import com.taxgap.entity.AuditLog;
import com.taxgap.enums.EventType;
import com.taxgap.repository.AuditLogRepository;

import java.util.ArrayList;
import java.util.List;

/** Simple in-memory fake of AuditLogRepository for tests. */
public class FakeAuditLogRepository extends FakeJpaRepository<AuditLog, Long>
        implements AuditLogRepository {

    public final List<AuditLog> store = new ArrayList<>();

    @Override
    public <S extends AuditLog> S save(S entity) {
        store.add(entity);
        return entity;
    }

    @Override
    public List<AuditLog> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public List<AuditLog> findByTransactionIdOrderByTimestampAsc(String transactionId) {
        return new ArrayList<>();
    }

    @Override
    public List<AuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType) {
        return new ArrayList<>();
    }
}
