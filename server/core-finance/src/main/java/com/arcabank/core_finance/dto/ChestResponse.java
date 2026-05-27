package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ChestResponse(
    UUID id,
    String name,
    BigDecimal targetAmount,
    String status
) {
}
