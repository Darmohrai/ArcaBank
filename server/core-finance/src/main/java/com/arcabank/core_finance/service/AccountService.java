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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    public List<AccountDto> getAccountsByUserId(UUID userId) {
        return accountMapper.toDtoList(
            accountRepository.findAllByUserId(userId)
        );
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
                    throw new AppException("Unable to create a unique card after 3 attempts", "CARD_GENERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (Exception ex) {
                log.error("An error occurred while creating the account", ex);
                throw new AppException("Internal server error while creating an account", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        throw new AppException("An unexpected error", "UNKNOWN_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CardDto getCardById(UUID cardId, UUID userId) {
        CardDto card = accountRepository.findCardByIdAndUserId(cardId, userId);

        if (card == null) {
            throw new AppException("Card not found or access denied", "CARD_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return card;
    }

    public List<CardDto> getAllCardsByUserId(UUID userId) {
        return accountRepository.findAllCardsByUserId(userId);
    }

    public AccountDto getAccountById(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
            .map(accountMapper::toDto)
            .orElseThrow(() -> new AppException(
                "Account not found or access denied",
                "ACCOUNT_NOT_FOUND",
                HttpStatus.NOT_FOUND
            ));
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
            .orElseThrow(() -> new AppException("Account not found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        UserResponse user = userClient.getUserById(userId);
        String cardHolderName = TransliterationUtil.formatCardHolderName(user.firstName(), user.lastName());

        String pan = BankDataGenerator.generatePan();
        String expDate = BankDataGenerator.generateExpirationDate();
        String cvvHash = passwordEncoder.encode(BankDataGenerator.generateCvv());
        String pinHash = passwordEncoder.encode(request.pin());

        UUID cardId = accountRepository.createJustCard(
            accountId, pan, cardHolderName, expDate, cvvHash, pinHash
        );

        return CardDto.builder()
            .id(cardId)
            .accountId(accountId)
            .cardNumber(pan)
            .cardHolderName(cardHolderName)
            .expirationDate(expDate)
            .status("ACTIVE")
            .build();
    }
}
