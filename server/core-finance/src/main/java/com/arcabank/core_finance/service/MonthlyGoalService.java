package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.MonthlyGoalProgressResponse;
import com.arcabank.core_finance.dto.MonthlyGoalRequest;
import com.arcabank.core_finance.dto.MonthlyStatsDto;
import com.arcabank.core_finance.model.MonthlyGoal;
import com.arcabank.core_finance.repository.MonthlyGoalRepository;
import com.arcabank.core_finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyGoalService {

    private final MonthlyGoalRepository monthlyGoalRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public MonthlyGoalProgressResponse setMonthlyGoal(UUID userId, MonthlyGoalRequest request) {
        UUID goalId = monthlyGoalRepository.upsertGoal(
            userId, request.year(), request.month(), request.targetAmount()
        );
        log.info("Monthly goal upserted for user {} for {}/{}", userId, request.month(), request.year());

        return getGoalProgress(userId, request.year(), request.month());
    }

    @Transactional(readOnly = true)
    public MonthlyGoalProgressResponse getGoalProgress(UUID userId, int year, int month) {

        MonthlyGoal goal = monthlyGoalRepository.findByUserIdAndYearAndMonth(userId, year, month)
            .orElse(MonthlyGoal.builder()
                .targetAmount(BigDecimal.ZERO)
                .year(year)
                .month(month)
                .build());

        MonthlyStatsDto stats = transactionRepository.getStatsForSpecificMonth(userId, year, month);

        BigDecimal income = BigDecimal.valueOf(stats.getIncome());
        BigDecimal expense = BigDecimal.valueOf(stats.getExpense());

        BigDecimal netIncome = income.subtract(expense);

        BigDecimal remaining = goal.getTargetAmount().subtract(netIncome);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return MonthlyGoalProgressResponse.builder()
            .goalId(goal.getId())
            .year(year)
            .month(month)
            .targetAmount(goal.getTargetAmount())
            .totalIncome(income)
            .totalExpense(expense)
            .netIncome(netIncome)
            .remainingToGoal(remaining)
            .build();
    }
}
