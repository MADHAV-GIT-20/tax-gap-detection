package com.taxgap.service;

import com.taxgap.domain.enums.ValidationStatus;
import com.taxgap.dto.BatchUploadRequest;
import com.taxgap.dto.BatchUploadResponse;
import com.taxgap.dto.TransactionDto;
import com.taxgap.dto.TransactionResultDto;
import com.taxgap.service.TransactionItemProcessor.ProcessOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the batch: delegates each transaction to {@link TransactionItemProcessor}
 * (its own transaction) and aggregates the batch-level response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingService {

    private final TransactionItemProcessor itemProcessor;

    public BatchUploadResponse process(BatchUploadRequest request) {
        List<TransactionResultDto> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;
        int exceptionsRaised = 0;

        for (TransactionDto dto : request.transactions()) {
            ProcessOutcome outcome = itemProcessor.processOne(dto);
            results.add(TransactionResultDto.from(outcome.transaction()));
            if (outcome.transaction().getValidationStatus() == ValidationStatus.SUCCESS) {
                succeeded++;
            } else {
                failed++;
            }
            exceptionsRaised += outcome.exceptionCount();
        }

        log.info("Batch processed: received={}, succeeded={}, failed={}, exceptions={}",
                request.transactions().size(), succeeded, failed, exceptionsRaised);

        return new BatchUploadResponse(
                request.transactions().size(), succeeded, failed, exceptionsRaised, results);
    }
}
