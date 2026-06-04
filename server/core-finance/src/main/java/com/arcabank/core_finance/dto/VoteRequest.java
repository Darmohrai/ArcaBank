package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(
    @NotNull(message = "Approval is required")
    Boolean decision
) {
}
