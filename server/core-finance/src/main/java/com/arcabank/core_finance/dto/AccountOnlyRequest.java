package com.arcabank.core_finance.dto;

import com.arcabank.core_finance.model.util.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountOnlyRequest(
    @NotBlank @Pattern(regexp = "UAH|USD|EUR") String currency,
    @NotNull AccountType type
) {}
