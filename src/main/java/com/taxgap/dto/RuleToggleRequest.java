package com.taxgap.dto;

import jakarta.validation.constraints.NotNull;

public record RuleToggleRequest(
        @NotNull(message = "enabled is required")
        Boolean enabled
) {
}
