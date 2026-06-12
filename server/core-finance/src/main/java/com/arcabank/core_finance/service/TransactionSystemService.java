package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.DepositRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSystemService {

    private final AccountRepository accountRepository;

    @Transactional
    public void processDeposit(DepositRequest request, String userId) {
        log.info("Processing deposit. Target account: {}, Amount: {}", request.accountId(), request.amount());

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Сума поповнення має бути більшою за нуль");
        }

        UUID userUuid = UUID.fromString(userId);

        Account targetAccount = accountRepository.findByIdAndUserId(request.accountId(), userUuid)
            .orElseThrow(() -> {
                log.warn("Security Alert: User {} attempted to access account {}", userId, request.accountId());
                return new AppException(ErrorCode.ACCESS_DENIED, "Рахунок не знайдено або ви не є його власником");
            });

        accountRepository.increaseBalance(targetAccount.getId(), request.amount());

        log.info("Deposit successful. Added {} UAH/USD/EUR to account {}", request.amount(), targetAccount.getId());
    }
}
