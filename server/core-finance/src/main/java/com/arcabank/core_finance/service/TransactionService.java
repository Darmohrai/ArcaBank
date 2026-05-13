package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.notificator.engine.Notificator;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.TransactionRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Notificator notificator;

    @Transactional
    public UUID processInternalTransfer(UUID userId, TransferRequest request) {

        String cleanDestination = request.destination().replaceAll("\\s+", "");

        if (request.sourceType() == SourceType.ACCOUNT && isCardFormat(cleanDestination)) {
            throw new AppException(ErrorCode.RESTRICTED_ROUTE, "З рахунку не можна на картку. Використовуйте IBAN.");
        }

        UUID senderAccountId = resolveSenderAccountId(userId, request);
        UUID receiverAccountId = resolveDestinationToAccountId(cleanDestination);

        Account senderAccount = accountRepository.findById(senderAccountId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок відправника не знайдено"));

        String currency = senderAccount.getCurrency().name();

        if (senderAccount.getBalance().compareTo(request.amount()) < 0) {
            notificator.notifyNotEnoughMoney(userId, request.amount(), currency);
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        try {
            UUID transactionId = accountRepository.processTransfer(senderAccountId, receiverAccountId, request.amount());

            Account receiverAccount = accountRepository.findById(receiverAccountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок отримувача не знайдено"));

            notificator.notifyTransferSuccess(
                senderAccount.getUserId(),
                receiverAccount.getUserId(),
                request.amount(),
                currency
            );

            return transactionId;

        } catch (AppException e) {
            notificator.notifyPaymentFailed(userId, request.amount(), currency, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            notificator.notifyPaymentFailed(userId, request.amount(), currency, "Технічний збій на боці банку");
            throw new AppException(ErrorCode.TRANSFER_ERROR);
        }
    }

    private UUID resolveSenderAccountId(UUID userId, TransferRequest request) {
        if (request.sourceType() == SourceType.CARD) {
            CardDto card = accountRepository.findCardByIdAndUserId(request.senderSourceId(), userId);
            if (card == null) {
                throw new AppException(ErrorCode.CARD_NOT_FOUND, "Картку відправника не знайдено або доступ заборонено");
            }
            return card.accountId();
        } else {
            Account account = accountRepository.findByIdAndUserId(request.senderSourceId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок відправника не знайдено"));
            return account.getId();
        }
    }

    private UUID resolveDestinationToAccountId(String destination) {
        if (isCardFormat(destination)) {
            return accountRepository.findAccountIdByCardNumber(destination)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND, "Картку отримувача не знайдено"));
        } else if (isIbanFormat(destination)) {
            return accountRepository.findAccountIdByIban(destination)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "IBAN отримувача не знайдено"));
        } else {
            throw new AppException(ErrorCode.INVALID_DESTINATION, "Невірний формат призначення. Використовуйте 16 цифр картки або IBAN.");
        }
    }

    private boolean isCardFormat(String text) {
        return text.matches("^\\d{16}$");
    }

    private boolean isIbanFormat(String text) {
        return text.matches("^UA\\d{27}$");
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getTransactionHistory(UUID accountId, UUID userId, int page, int size) {

        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Рахунок не знайдено або доступ заборонено"));

        int offset = page * size;
        List<TransactionDto> rawTransactions = transactionRepository.findTransactionsByAccountId(accountId, size, offset);
        long totalElements = transactionRepository.countTransactionsByAccountId(accountId);

        int totalPages = (int) ((totalElements + size - 1) / size);

        List<TransactionDto> enrichedTransactions = rawTransactions.stream()
            .map(t -> {
                String type = accountId.equals(t.senderAccountId()) ? "EXPENSE" : "INCOME";
                return TransactionDto.builder()
                    .id(t.id())
                    .senderAccountId(t.senderAccountId())
                    .receiverAccountId(t.receiverAccountId())
                    .amount(t.amount())
                    .currency(t.currency())
                    .status(t.status())
                    .createdAt(t.createdAt())
                    .type(type)
                    .build();
            })
            .toList();

        return new PageResponse<>(enrichedTransactions, page, size, totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getAllUserTransactionHistory(UUID userId, int page, int size) {
        int offset = page * size;

        List<TransactionDto> rawTransactions = transactionRepository.findAllTransactionsByUserId(userId, size, offset);
        long totalElements = transactionRepository.countAllTransactionsByUserId(userId);

        int totalPages = (int) ((totalElements + size - 1) / size);

        List<UUID> userAccountIds = accountRepository.findAllByUserId(userId).stream()
            .map(Account::getId)
            .toList();

        List<TransactionDto> enrichedTransactions = rawTransactions.stream()
            .map(t -> {
                String type;
                if (userAccountIds.contains(t.senderAccountId()) && userAccountIds.contains(t.receiverAccountId())) {
                    type = "INTERNAL";
                }
                else if (userAccountIds.contains(t.senderAccountId())) {
                    type = "EXPENSE";
                }
                else {
                    type = "INCOME";
                }

                return TransactionDto.builder()
                    .id(t.id())
                    .senderAccountId(t.senderAccountId())
                    .receiverAccountId(t.receiverAccountId())
                    .amount(t.amount())
                    .currency(t.currency())
                    .status(t.status())
                    .createdAt(t.createdAt())
                    .type(type)
                    .build();
            })
            .toList();

        return new PageResponse<>(enrichedTransactions, page, size, totalElements, totalPages);
    }
}
