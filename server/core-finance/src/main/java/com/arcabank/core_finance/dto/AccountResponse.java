package com.arcabank.core_finance.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record AccountResponse(
    UUID accountId,
    UUID cardId,
    String iban,
    String cardNumber,
    String cardHolderName,
    String expirationDate,
    String cvv,
    String currency,
    BigDecimal balance
) {
}
