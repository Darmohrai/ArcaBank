package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.ExchangeRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.ExchangeRate;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final AccountRepository accountRepository;
    private final ExchangeRateRepository rateRepository;

    @Transactional
    public UUID processExchange(UUID userId, ExchangeRequest request) {
        Account fromAccount = accountRepository.findByIdAndUserId(request.fromAccountId(), userId)
            .orElseThrow(() -> new AppException("Source account not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        Account toAccount = accountRepository.findByIdAndUserId(request.toAccountId(), userId)
            .orElseThrow(() -> new AppException("Destination account not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        String fromCurr = fromAccount.getCurrency().name();
        String toCurr = toAccount.getCurrency().name();

        if (fromCurr.equals(toCurr)) {
            throw new AppException("Currencies must be different", "SAME_CURRENCY", HttpStatus.BAD_REQUEST);
        }

        BigDecimal amountFrom = request.amount();
        BigDecimal amountTo;
        BigDecimal appliedRate;

        if (fromCurr.equals("UAH")) {
            ExchangeRate rate = rateRepository.findByCurrency(toCurr)
                .orElseThrow(() -> new AppException("Rate not found", "RATE_ERROR", HttpStatus.BAD_REQUEST));

            appliedRate = rate.getSellRate();
            amountTo = amountFrom.divide(appliedRate, 2, RoundingMode.HALF_DOWN);

        } else if (toCurr.equals("UAH")) {
            ExchangeRate rate = rateRepository.findByCurrency(fromCurr)
                .orElseThrow(() -> new AppException("Rate not found", "RATE_ERROR", HttpStatus.BAD_REQUEST));

            appliedRate = rate.getBuyRate();
            amountTo = amountFrom.multiply(appliedRate).setScale(2, RoundingMode.HALF_DOWN);

        } else {
            throw new AppException("Cross-currency exchange not supported in MVP", "UNSUPPORTED", HttpStatus.BAD_REQUEST);
        }

        return accountRepository.processExchangeProcedure(
            fromAccount.getId(), toAccount.getId(), amountFrom, amountTo, appliedRate
        );
    }
}
