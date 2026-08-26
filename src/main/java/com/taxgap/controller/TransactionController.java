package com.taxgap.controller;

import com.taxgap.dto.BatchUploadRequest;
import com.taxgap.dto.BatchUploadResponse;
import com.taxgap.dto.TransactionView;
import com.taxgap.service.TransactionProcessingService;
import com.taxgap.service.TransactionQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionProcessingService processingService;
    private final TransactionQueryService queryService;

    /** Upload & validate a batch of financial transactions. */
    @PostMapping("/batch")
    public ResponseEntity<BatchUploadResponse> upload(@Valid @RequestBody BatchUploadRequest request) {
        BatchUploadResponse response = processingService.process(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** List stored transactions, optionally filtered by customerId. */
    @GetMapping
    public List<TransactionView> list(@RequestParam(required = false) String customerId) {
        return queryService.findAll(customerId).stream()
                .map(TransactionView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TransactionView getOne(@PathVariable Long id) {
        return TransactionView.from(queryService.findById(id));
    }
}
