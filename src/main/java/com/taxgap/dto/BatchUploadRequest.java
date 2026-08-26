package com.taxgap.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchUploadRequest(
        @NotEmpty(message = "transactions batch must not be empty")
        List<TransactionDto> transactions
) {
}
