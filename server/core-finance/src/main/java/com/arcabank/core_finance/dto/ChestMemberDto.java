package com.arcabank.core_finance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChestMemberDto(
    UUID userId,
    String role,
    LocalDateTime joinedAt
) {}
