package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingEscrowResponse(
    UUID id,
    UUID chestId,
    UUID initiatorId,
    BigDecimal amount,
    UUID destinationAccount,
    String purpose,
    String status,
    LocalDateTime createdAt,
    int approvalsCount,
    int trusteesCount,
    boolean currentUserVoted,
    boolean canCurrentUserVote
) {}
