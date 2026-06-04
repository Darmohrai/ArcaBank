package com.arcabank.core_finance.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowVote {
    private UUID id;
    private UUID escrowTransactionId;
    private UUID userId;
    private String decision;
    private LocalDateTime createdAt;
}
