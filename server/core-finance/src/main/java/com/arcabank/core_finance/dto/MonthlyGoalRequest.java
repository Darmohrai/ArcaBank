package com.arcabank.core_finance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MonthlyGoalRequest(
    @NotNull(message = "Рік є обов'язковим")
    Integer year,

    @NotNull(message = "Місяць є обов'язковим")
    @Min(value = 1, message = "Місяць має бути від 1 до 12")
    @Max(value = 12, message = "Місяць має бути від 1 до 12")
    Integer month,

    @NotNull(message = "Цільова сума є обов'язковою")
    @Positive(message = "Сума має бути більшою за нуль")
    BigDecimal targetAmount
) {}
