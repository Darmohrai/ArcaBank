package com.arcabank.core_finance.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ChestDetailResponse(
    UUID id,
    String name,
    BigDecimal targetAmount,
    BigDecimal balance,
    String currency,
    String status,
    List<ChestMemberDto> members,
    List<PendingEscrowResponse> escrows
) {}
