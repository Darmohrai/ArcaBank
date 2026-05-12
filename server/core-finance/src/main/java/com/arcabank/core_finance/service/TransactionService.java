package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.CardDto;
import com.arcabank.core_finance.dto.SourceType;
import com.arcabank.core_finance.dto.TransferRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;

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
            if (card == null) {
                throw new AppException("Картку відправника не знайдено або доступ заборонено", "CARD_NOT_FOUND", HttpStatus.NOT_FOUND);
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
}
