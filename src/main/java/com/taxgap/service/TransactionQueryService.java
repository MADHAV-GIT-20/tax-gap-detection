package com.taxgap.service;

import com.taxgap.domain.Transaction;
import com.taxgap.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Transaction> findAll(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return transactionRepository.findAll();
        }
        return transactionRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
    }
}
