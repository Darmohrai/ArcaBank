package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ChestDepositRequest(

    @NotNull(message = "Account Id must be not blank")
    UUID senderAccountId,

    @NotNull(message = "Amount must be not blank")
    @Positive(message = "Amount must be positive")
    BigDecimal amount

) {
}
