package com.arcabank.core_finance.model;

import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.AccountType;
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
public class Account {
    private UUID id;
    private UUID userId;
    private String iban;
    private AccountType type;
    private Currency currency;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountStatus status = AccountStatus.ACTIVE;
    private LocalDateTime createdAt;
}
