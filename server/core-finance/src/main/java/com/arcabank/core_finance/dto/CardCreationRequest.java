package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardCreationRequest(
    @NotBlank @Pattern(regexp = "^\\d{4}$") String pin
) {}
