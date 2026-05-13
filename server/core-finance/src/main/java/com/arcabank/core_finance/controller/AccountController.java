package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.service.AccountService;
import com.arcabank.core_finance.service.TransactionService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(RoutingRegistry.Api.Accounts.BASE)
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping(RoutingRegistry.Api.Accounts.ALL)
    public ResponseEntity<List<AccountDto>> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userIdString = jwt.getSubject();
        UUID userId = UUID.fromString(userIdString);

        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);

        return ResponseEntity.ok(accounts);
    }

    @PostMapping(RoutingRegistry.Api.Accounts.CREATE_WITH_CARD)
    public ResponseEntity<AccountResponse> createAccount(
        @Valid @RequestBody AccountCreationRequest request,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        AccountResponse response = accountService.createAccountWithCard(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
        @Valid @RequestBody AccountOnlyRequest request,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        AccountDto account = accountService.openNewAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping(RoutingRegistry.Api.Accounts.BY_ID)
    public ResponseEntity<AccountDto> getAccountById(
        @PathVariable("id") UUID id,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        AccountDto account = accountService.getAccountById(id, userId);

        return ResponseEntity.ok(account);
    }

    @GetMapping(RoutingRegistry.Api.Accounts.ACCOUNT_TRANSACTIONS)
    public ResponseEntity<PageResponse<TransactionDto>> getAccountTransactions(
        @PathVariable("accountId") UUID accountId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PageResponse<TransactionDto> response = transactionService.getTransactionHistory(accountId, userId, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping(RoutingRegistry.Api.Accounts.ACCOUNT_CARDS) // Шлях: /api/v1/accounts/{accountId}/cards
    public ResponseEntity<List<CardDto>> getCardsByAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<CardDto> cards = accountService.getCardsByAccountId(accountId, userId);

        return ResponseEntity.ok(cards);
    }

    @PatchMapping(RoutingRegistry.Api.Accounts.BLOCK)
    public ResponseEntity<Map<String, String>> blockAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.blockAccount(accountId, userId);

        return ResponseEntity.ok(Map.of("message", "Account successfully blocked"));
    }

    @PatchMapping(RoutingRegistry.Api.Accounts.UNBLOCK)
    public ResponseEntity<Map<String, String>> unblockAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.unblockAccount(accountId, userId);

        return ResponseEntity.ok(Map.of("message", "Account successfully unblocked"));
    }
}
