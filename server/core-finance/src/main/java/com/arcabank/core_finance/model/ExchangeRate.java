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
public class ExchangeRate {
    private UUID id;
    private String currency;
    private String baseCurrency;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDateTime updatedAt;
}
