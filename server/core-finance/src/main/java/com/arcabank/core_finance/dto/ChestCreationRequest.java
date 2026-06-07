package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record ChestCreationRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotNull(message = "Target amount cannot be null")
    @Positive(message = "Target amount must be positive")
    BigDecimal targetAmount,

    String description,

    List<String> friendPhones,
    String currency,
    String pin
) {
}
