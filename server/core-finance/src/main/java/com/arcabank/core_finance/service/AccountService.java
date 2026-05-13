package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.convertor.AccountMapper;
import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.Currency;
import com.arcabank.core_finance.notificator.engine.Notificator;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.model.util.BankDataGenerator;
import com.arcabank.core_finance.model.util.TransliterationUtil;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;

    private final Notificator notificator;

    private static final int MAX_RETRIES = 3;

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(UUID userId) {
        return accountRepository.findAllByUserId(userId).stream()
            .map(accountMapper::toDto)
            .toList();
    }

    // 1. Method for gRPC: The data is already there—no need for Feign!
    @Transactional
    public AccountResponse createAccountWithCard(UUID userId, AccountCreationRequest request, String firstName, String lastName) {

        String cardHolderName = TransliterationUtil.formatCardHolderName(firstName, lastName);

        return generateAndSaveAccount(userId, request, cardHolderName);
    }

    // 2. Old method: Used for the REST Controller (when the user creates the second card themselves)
    @Transactional
    public AccountResponse createAccountWithCard(UUID userId, AccountCreationRequest request) {

        UserResponse user = userClient.getUserById(userId);

        String cardHolderName = TransliterationUtil.formatCardHolderName(user.firstName(), user.lastName());

        return generateAndSaveAccount(userId, request, cardHolderName);
    }

    private AccountResponse generateAndSaveAccount(UUID userId, AccountCreationRequest request, String cardHolderName) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String iban = BankDataGenerator.generateIban();
                String pan = BankDataGenerator.generatePan();
                String expDate = BankDataGenerator.generateExpirationDate();

                String rawCvv = BankDataGenerator.generateCvv();
                String cvvHash = passwordEncoder.encode(rawCvv);

                String pinHash = passwordEncoder.encode(request.pin());

                log.info("Try {}: Let's create a card for {}", attempt, cardHolderName);

                Map<String, UUID> ids = accountRepository.callCreateAccountWithCard(
                    userId, request.type().name(), request.currency(),
                    iban, pan, cardHolderName, expDate, cvvHash, pinHash
                );

                UUID accountId = ids.get("account_id");
                UUID cardId = ids.get("cardId");

                notificator.notifyCardCreated(userId, cardId, pan);

                return AccountResponse.builder()
                    .accountId(accountId)
                    .cardId(cardId)
                    .iban(iban)
                    .cardNumber(pan)
                    .cardHolderName(cardHolderName)
                    .expirationDate(expDate)
                    .cvv(rawCvv)
                    .currency(request.currency())
                    .balance(BigDecimal.ZERO)
                    .build();

            } catch (DataIntegrityViolationException ex) {
                log.warn("The card number or IBAN already exists. Let's try again... (Attempt {}/{})", attempt, MAX_RETRIES);
                if (attempt == MAX_RETRIES) {
                    throw new AppException(ErrorCode.CARD_GENERATION_FAILED);
                }
            } catch (Exception ex) {
                log.error("An error occurred while creating the account", ex);
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Internal server error while creating an account");
            }
        }

        throw new AppException(ErrorCode.INTERNAL_ERROR, "An unexpected error");
    }

    @Transactional(readOnly = true)
    public CardDto getCardById(UUID cardId, UUID userId) {
        CardDto card = accountRepository.findCardByIdAndUserId(cardId, userId);

        if (card == null) {
            throw new AppException(ErrorCode.CARD_NOT_FOUND);
        }

        return card;
    }

    @Transactional(readOnly = true)
    public List<CardDto> getAllCardsByUserId(UUID userId) {
        return accountRepository.findAllCardsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
            .map(accountMapper::toDto)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Transactional
    public AccountDto openNewAccount(UUID userId, AccountOnlyRequest request) {
        String iban = BankDataGenerator.generateIban();

        Account account = Account.builder()
            .userId(userId)
            .iban(iban)
            .type(request.type())
            .currency(Currency.valueOf(request.currency()))
            .status(AccountStatus.ACTIVE)
            .build();

        UUID accountId = accountRepository.createJustAccount(account);
        account.setId(accountId);

        notificator.notifyAccountCreated(account);

        return accountMapper.toDto(account);
    }

    @Transactional
    public CardDto issueCardForAccount(UUID userId, UUID accountId, CardCreationRequest request) {
        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        UserResponse user = userClient.getUserById(userId);
        String cardHolderName = TransliterationUtil.formatCardHolderName(user.firstName(), user.lastName());

        String pan = BankDataGenerator.generatePan();
        String expDate = BankDataGenerator.generateExpirationDate();
        String cvvHash = passwordEncoder.encode(BankDataGenerator.generateCvv());
        String pinHash = passwordEncoder.encode(request.pin());

        UUID cardId = accountRepository.createJustCard(
            accountId, pan, cardHolderName, expDate, cvvHash, pinHash
        );

        notificator.notifyCardCreated(userId, cardId, pan);

        return CardDto.builder()
            .id(cardId)
            .accountId(accountId)
            .cardNumber(pan)
            .cardHolderName(cardHolderName)
            .expirationDate(expDate)
            .status("ACTIVE")
            .build();
    }

    @Transactional(readOnly = true)
    public List<CardDto> getCardsByAccountId(UUID accountId, UUID userId) {
        accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        return accountRepository.findAllCardsByAccountId(accountId);
    }

    @Transactional
    public void blockAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AppException(ErrorCode.ALREADY_BLOCKED);
        }

        accountRepository.updateAccountStatus(accountId, AccountStatus.BLOCKED);
        account.setStatus(AccountStatus.BLOCKED);

        notificator.notifyAccountBlocked(account);
        log.info("Account {} was BLOCKED by user {}", accountId, userId);
    }

    @Transactional
    public void unblockAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new AppException(ErrorCode.ALREADY_ACTIVE);
        }

        accountRepository.updateAccountStatus(accountId, AccountStatus.ACTIVE);
        account.setStatus(AccountStatus.ACTIVE);

        notificator.notifyAccountUnblocked(account);
        log.info("Account {} was UNBLOCKED by user {}", accountId, userId);
    }

    @Transactional
    public void blockCard(UUID cardId, UUID userId) {
        CardDto card = getCardById(cardId, userId);

        if ("BLOCKED".equals(card.status())) {
            throw new AppException(ErrorCode.ALREADY_BLOCKED);
        }

        accountRepository.updateCardStatus(cardId, "BLOCKED");

        notificator.notifyCardBlocked(userId, cardId, card.cardNumber());
        log.info("Card {} was BLOCKED by user {}", cardId, userId);
    }

    @Transactional
    public void unblockCard(UUID cardId, UUID userId) {
        CardDto card = getCardById(cardId, userId);

        if ("ACTIVE".equals(card.status())) {
            throw new AppException(ErrorCode.ALREADY_ACTIVE);
        }

        accountRepository.updateCardStatus(cardId, "ACTIVE");

        notificator.notifyCardUnblocked(userId, cardId, card.cardNumber());
        log.info("Card {} was UNBLOCKED by user {}", cardId, userId);
    }
}
