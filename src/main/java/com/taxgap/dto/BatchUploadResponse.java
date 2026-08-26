package com.taxgap.dto;

import java.util.List;

public record BatchUploadResponse(
        int received,
        int succeeded,
        int failed,
        int exceptionsRaised,
        List<TransactionResultDto> results
) {
}
