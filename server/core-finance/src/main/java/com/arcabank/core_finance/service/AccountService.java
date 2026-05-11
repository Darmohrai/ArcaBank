package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.convertor.AccountMapper;
import com.arcabank.core_finance.dto.AccountCreationRequest;
import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.dto.AccountResponse;
import com.arcabank.core_finance.dto.UserResponse;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.model.util.BankDataGenerator;
import com.arcabank.core_finance.model.util.TransliterationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private static final int MAX_RETRIES = 3;

    public List<AccountDto> getAccountsByUserId(UUID userId) {
        return accountMapper.toDtoList(
            accountRepository.findAllByUserId(userId)
        );
    }

    // 1. Метод для gRPC: Дані вже є, Feign не потрібен!
    public AccountResponse createAccountWithCard(UUID userId, AccountCreationRequest request, String firstName, String lastName) {

        String cardHolderName = TransliterationUtil.formatCardHolderName(firstName, lastName);

        return generateAndSaveAccount(userId, request, cardHolderName);
    }

    // 2. Старий метод: Використовується для REST Controller (коли юзер сам створює 2-гу картку)
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

                log.info("Try {}: Let's create a card for {}", attempt, cardHolderName);

                Map<String, UUID> ids = accountRepository.callCreateAccountWithCard(
                    userId, request.type().name(), request.currency(),
                    iban, pan, cardHolderName, expDate, cvvHash
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
}
