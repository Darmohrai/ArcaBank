package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.notificator.engine.Notificator;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
            throw new AppException(
                "З прямого рахунку не можна переказувати на картку. Використовуйте IBAN.",
                "RESTRICTED_TRANSFER_ROUTE",
                HttpStatus.BAD_REQUEST
            );
        }

        UUID senderAccountId = resolveSenderAccountId(userId, request);

        UUID receiverAccountId = resolveDestinationToAccountId(cleanDestination);

        try {
            UUID transactionId = accountRepository.processTransfer(senderAccountId, receiverAccountId, request.amount());

            Account senderAccount = accountRepository.findById(senderAccountId).orElseThrow();
            Account receiverAccount = accountRepository.findById(receiverAccountId).orElseThrow();

            notificator.notifyTransferSuccess(
                senderAccount.getUserId(),
                receiverAccount.getUserId(),
                request.amount(),
                senderAccount.getCurrency().name()
            );

            log.info("Process transfer: SourceAccount: {}, TargetAccount: {}, Amount: {}", senderAccountId, receiverAccountId, request.amount());
            return accountRepository.processTransfer(senderAccountId, receiverAccountId, request.amount());
        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            throw new AppException("Transfer failed: " + e.getMessage(), "TRANSFER_ERROR", HttpStatus.BAD_REQUEST);
        }
    }

    private UUID resolveSenderAccountId(UUID userId, TransferRequest request) {
        if (request.sourceType() == SourceType.CARD) {
            CardDto card = accountRepository.findCardByIdAndUserId(request.senderSourceId(), userId);

            if (card == null || !"ACTIVE".equals(card.status())) {
                throw new AppException("Картку відправника не знайдено або вона неактивна", "CARD_NOT_ACTIVE", HttpStatus.BAD_REQUEST);
            }
            return card.accountId();
        } else {
            Account account = accountRepository.findByIdAndUserId(request.senderSourceId(), userId)
                .orElseThrow(() -> new AppException("Рахунок відправника не знайдено", "ACCOUNT_NOT_FOUND", HttpStatus.NOT_FOUND));
            return account.getId();
        }
    }

    private UUID resolveDestinationToAccountId(String destination) {
        if (isCardFormat(destination)) {
            return accountRepository.findAccountIdByCardNumber(destination)
                .orElseThrow(() -> new AppException("Картку отримувача не знайдено", "RECEIVER_CARD_NOT_FOUND", HttpStatus.NOT_FOUND));
        } else if (isIbanFormat(destination)) {
            return accountRepository.findAccountIdByIban(destination)
                .orElseThrow(() -> new AppException("IBAN отримувача не знайдено", "RECEIVER_IBAN_NOT_FOUND", HttpStatus.NOT_FOUND));
        } else {
            throw new AppException("Невірний формат призначення. Використовуйте 16 цифр картки або IBAN.", "INVALID_DESTINATION", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isCardFormat(String text) {
        return text.matches("^\\d{16}$");
    }

    private boolean isIbanFormat(String text) {
        return text.matches("^UA\\d{27}$");
    }

    public PageResponse<TransactionDto> getTransactionHistory(UUID accountId, UUID userId, int page, int size) {

        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException("Рахунок не знайдено або доступ заборонено", "ACCOUNT_NOT_FOUND", HttpStatus.NOT_FOUND));

        int offset = page * size;
        List<TransactionDto> rawTransactions = transactionRepository.findTransactionsByAccountId(accountId, size, offset);
        long totalElements = transactionRepository.countTransactionsByAccountId(accountId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

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
}
