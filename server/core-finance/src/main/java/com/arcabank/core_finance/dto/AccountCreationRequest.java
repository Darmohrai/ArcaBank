package com.arcabank.core_finance.dto;

import com.arcabank.core_finance.model.util.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountCreationRequest(
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "UAH|USD|EUR", message = "Only UAH, USD, or EUR are accepted")
    String currency,

    @NotNull(message = "Account type is required")
    AccountType type
) {}
