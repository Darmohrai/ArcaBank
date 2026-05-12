package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
    @NotNull(message = "Sender Source ID is required")
    UUID senderSourceId,

    @NotNull(message = "Source Type is required")
    SourceType sourceType,

    @NotBlank(message = "Destination is required")
    String destination,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount
) {}
