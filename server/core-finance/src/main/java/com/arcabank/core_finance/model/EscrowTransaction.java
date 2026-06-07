package com.arcabank.core_finance.model;

import com.arcabank.core_finance.model.util.EscrowStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowTransaction {
    private UUID id;
    private UUID chestId;
    private UUID initiatorId;
    private BigDecimal amount;
    private UUID destinationAccountId;
    private String purpose;
    private EscrowStatus status;
    private LocalDateTime createdAt;
}
