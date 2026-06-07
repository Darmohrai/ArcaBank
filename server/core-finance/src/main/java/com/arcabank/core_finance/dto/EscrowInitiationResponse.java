package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EscrowInitiationResponse(
    String message,
    UUID escrowId,
    UUID chestId,
    BigDecimal amount,
    String status
) {}
