package com.arcabank.core_finance.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionDto(
    UUID id,
    UUID senderAccountId,
    UUID receiverAccountId,
    BigDecimal amount,
    String currency,
    String status,
    LocalDateTime createdAt,
    String type
) {}
