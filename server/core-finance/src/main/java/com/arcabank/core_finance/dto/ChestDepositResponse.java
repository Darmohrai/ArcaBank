package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChestDepositResponse(
    String message,
    UUID chestId,
    BigDecimal newBalance
) {
}
