package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExchangeRequest(
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount
) {}
