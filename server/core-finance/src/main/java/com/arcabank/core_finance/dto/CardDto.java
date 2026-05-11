package com.arcabank.core_finance.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record CardDto(
    UUID id,
    UUID accountId,
    String cardNumber,
    String cardHolderName,
    String expirationDate,
    String status
) {}
