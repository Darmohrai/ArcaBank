package com.arcabank.core_finance.model;

import com.arcabank.core_finance.model.util.ChestStatus;
import com.arcabank.core_finance.model.util.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chest {
    private UUID id;
    private UUID accountId;
    private String name;
    private BigDecimal targetAmount;
    private String description;
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private ChestStatus status;
    private LocalDateTime createdAt;
}
