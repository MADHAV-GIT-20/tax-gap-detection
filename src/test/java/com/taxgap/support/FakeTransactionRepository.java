package com.taxgap.support;

import com.taxgap.dto.CustomerSummaryDto;
import com.taxgap.entity.Transaction;
import com.taxgap.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Simple in-memory fake of TransactionRepository for tests. */
public class FakeTransactionRepository extends FakeJpaRepository<Transaction, Long>
        implements TransactionRepository {

    private final List<Transaction> store = new ArrayList<>();
    private long nextId = 1;

    /** Canned result for the report query (set by tests that need it). */
    public List<CustomerSummaryDto> summaryRows = new ArrayList<>();

    @Override
    public <S extends Transaction> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        store.add(entity);
        return entity;
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return store.stream().filter(t -> id.equals(t.getId())).findFirst();
    }

    @Override
    public List<Transaction> findByCustomerId(String customerId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : store) {
            if (customerId.equals(t.getCustomerId())) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public List<CustomerSummaryDto> customerTaxSummary() {
        return summaryRows;
    }
}
