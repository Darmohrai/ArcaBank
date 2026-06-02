package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record EscrowInitiationRequest(
    @NotNull @Positive(message = "The sum must be greater than zero")
    BigDecimal amount,

    @NotNull(message = "The destination account is required")
    UUID destinationAccount,

    @NotBlank(message = "Please specify the payment purpose")
    String purpose
) {
}
