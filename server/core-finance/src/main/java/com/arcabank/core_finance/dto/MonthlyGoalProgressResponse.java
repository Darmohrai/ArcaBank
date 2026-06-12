package com.arcabank.core_finance.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record MonthlyGoalProgressResponse(
    UUID goalId,
    int year,
    int month,
    BigDecimal targetAmount,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal netIncome,
    BigDecimal remainingToGoal
) {}
