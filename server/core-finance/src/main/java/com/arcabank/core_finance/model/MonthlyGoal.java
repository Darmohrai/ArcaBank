package com.arcabank.core_finance.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyGoal {
    private UUID id;
    private UUID userId;
    private int year;
    private int month;
    private BigDecimal targetAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
