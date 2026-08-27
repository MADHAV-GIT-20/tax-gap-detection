package com.taxgap.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** The request body for uploading a batch of transactions. */
public class BatchUploadRequest {

    @NotEmpty(message = "transactions must not be empty")
    private List<TransactionDto> transactions;

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }
}
