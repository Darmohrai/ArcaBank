package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.DepositRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSystemService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Value("${bank.system.account-id}")
    private UUID systemAccountId;

    @Transactional
    public void processDeposit(DepositRequest request, String userId) {
        log.info("Processing deposit. Target account: {}, Amount: {}", request.accountId(), request.amount());

        Account targetAccount = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new AppException("Account with ID " + request.accountId() + " not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!targetAccount.getUserId().toString().equals(userId)) {
            log.warn("Security Alert: User {} attempted to deposit to an account {} belonging to another user.", userId, targetAccount.getId());
            throw new AppException("Access Denied. You can only deposit funds to your own accounts.", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        BigDecimal newBalance = targetAccount.getBalance().add(request.amount());

        accountRepository.updateBalance(targetAccount.getId(), newBalance);

        transactionRepository.createTransaction(
            systemAccountId,
            targetAccount.getId(),
            request.amount(),
            targetAccount.getCurrency().name(),
            "SUCCESS"
        );

        log.info("Deposit successful. Account {} new balance: {}", targetAccount.getId(), newBalance);
    }
}
