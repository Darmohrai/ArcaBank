package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.ExchangeRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.ExchangeRate;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.ExchangeRateRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final AccountRepository accountRepository;
    private final ExchangeRateRepository rateRepository;

    @Transactional
    public UUID processExchange(UUID userId, ExchangeRequest request) {

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Сума обміну має бути більшою за нуль");
        }

        Account fromAccount = accountRepository.findByIdAndUserId(request.fromAccountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок списання не знайдено"));

        Account toAccount = accountRepository.findByIdAndUserId(request.toAccountId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок зарахування не знайдено"));

        String fromCurr = fromAccount.getCurrency().name();
        String toCurr = toAccount.getCurrency().name();

        if (fromCurr.equals(toCurr)) {
            throw new AppException(ErrorCode.SAME_CURRENCY);
        }

        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Not enough money for exchange. User: {}, Required: {}", userId, request.amount());
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        BigDecimal amountFrom = request.amount();
        BigDecimal amountTo;
        BigDecimal appliedRate;

        if (fromCurr.equals("UAH")) {
            ExchangeRate rate = rateRepository.findByCurrency(toCurr)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Курс валют тимчасово недоступний"));

            appliedRate = rate.getSellRate();
            amountTo = amountFrom.divide(appliedRate, 2, RoundingMode.HALF_EVEN);

        } else if (toCurr.equals("UAH")) {
            ExchangeRate rate = rateRepository.findByCurrency(fromCurr)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Курс валют тимчасово недоступний"));

            appliedRate = rate.getBuyRate();
            amountTo = amountFrom.multiply(appliedRate).setScale(2, RoundingMode.HALF_EVEN);

        } else {
            throw new AppException(ErrorCode.RESTRICTED_ROUTE, "Крос-курси (наприклад USD -> EUR) поки не підтримуються");
        }

        if (amountTo.compareTo(BigDecimal.ZERO) == 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Сума після конвертації надто мала (0.00)");
        }

        log.info("Processing exchange for user {}: {} {} -> {} {} (Rate: {})",
            userId, amountFrom, fromCurr, amountTo, toCurr, appliedRate);

        return accountRepository.processExchangeProcedure(
            fromAccount.getId(), toAccount.getId(), amountFrom, amountTo, appliedRate
        );
    }
}
