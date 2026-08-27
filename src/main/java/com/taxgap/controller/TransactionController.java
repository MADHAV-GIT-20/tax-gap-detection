package com.taxgap.controller;

import com.taxgap.dto.BatchUploadRequest;
import com.taxgap.dto.BatchUploadResponse;
import com.taxgap.entity.Transaction;
import com.taxgap.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Upload and validate a batch of transactions. */
    @PostMapping("/batch")
    public ResponseEntity<BatchUploadResponse> upload(@Valid @RequestBody BatchUploadRequest request) {
        BatchUploadResponse response = transactionService.process(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** List stored transactions, optionally filtered by customerId. */
    @GetMapping
    public List<Transaction> list(@RequestParam(required = false) String customerId) {
        return transactionService.findAll(customerId);
    }

    @GetMapping("/{id}")
    public Transaction getOne(@PathVariable Long id) {
        return transactionService.findById(id);
    }
}
