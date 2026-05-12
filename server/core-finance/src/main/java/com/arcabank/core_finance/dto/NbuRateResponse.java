package com.arcabank.core_finance.dto;

import java.math.BigDecimal;

public record NbuRateResponse(
    String cc,
    BigDecimal rate,
    String exchangedate
) {}
