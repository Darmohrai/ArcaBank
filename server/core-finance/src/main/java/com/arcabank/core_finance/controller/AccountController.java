package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.service.AccountService;
import com.arcabank.core_finance.service.PdfService;
import com.arcabank.core_finance.service.TransactionService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Accounts", description = "API for managing bank accounts and statements")
@RequiredArgsConstructor
@RestController
@RequestMapping(RoutingRegistry.Api.Accounts.BASE)
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PdfService pdfService;

    @Operation(summary = "Get all user accounts", description = "Returns a list of all accounts belonging to the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of accounts"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Accounts.ALL)
    public ResponseEntity<List<AccountDto>> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userIdString = jwt.getSubject();
        UUID userId = UUID.fromString(userIdString);

        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);

        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Create an account with a card", description = "Creates a new account and automatically issues a linked card.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account and card successfully created"),
        @ApiResponse(responseCode = "400", description = "Validation error of input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error (e.g., failed to generate IBAN/PAN)")
    })
    @PostMapping(RoutingRegistry.Api.Accounts.CREATE_WITH_CARD)
    public ResponseEntity<AccountResponse> createAccount(
        @Valid @RequestBody AccountCreationRequest request,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        AccountResponse response = accountService.createAccountWithCard(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create a new account", description = "Opens a new account without issuing a physical or virtual card.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account successfully created"),
        @ApiResponse(responseCode = "400", description = "Validation error of input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
        @Valid @RequestBody AccountOnlyRequest request,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        AccountDto account = accountService.openNewAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(summary = "Get account details", description = "Returns account information by its ID (only if it belongs to the user).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (account belongs to another user)"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Accounts.BY_ID)
    public ResponseEntity<AccountDto> getAccountById(
        @PathVariable("id") UUID id,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        AccountDto account = accountService.getAccountById(id, userId);

        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get transaction history", description = "Returns a paginated list of incoming and outgoing transactions for the specified account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get all account cards", description = "Returns a list of cards linked to a specific account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cards successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Accounts.ACCOUNT_CARDS)
    public ResponseEntity<List<CardDto>> getCardsByAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<CardDto> cards = accountService.getCardsByAccountId(accountId, userId);

        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Block account", description = "Blocks an active account. Debit operations will become unavailable.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully blocked"),
        @ApiResponse(responseCode = "400", description = "Account is already blocked"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(RoutingRegistry.Api.Accounts.BLOCK)
    public ResponseEntity<Map<String, String>> blockAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.blockAccount(accountId, userId);

        return ResponseEntity.ok(Map.of("message", "Account successfully blocked"));
    }

    @Operation(summary = "Unblock account", description = "Removes the block from an account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully unblocked"),
        @ApiResponse(responseCode = "400", description = "Account is already active"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(RoutingRegistry.Api.Accounts.UNBLOCK)
    public ResponseEntity<Map<String, String>> unblockAccount(
        @PathVariable("accountId") UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.unblockAccount(accountId, userId);

        return ResponseEntity.ok(Map.of("message", "Account successfully unblocked"));
    }

    @Operation(summary = "Download statement in PDF", description = "Generates and returns a PDF file with the transaction history for the account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF file successfully generated"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Error during PDF generation")
    })
    @GetMapping(value = RoutingRegistry.Api.Accounts.STATEMENT_PDF, produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getAccountStatementPdf(
        @PathVariable UUID accountId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        AccountDto account = accountService.getAccountById(accountId, userId);
        PageResponse<TransactionDto> history = transactionService.getTransactionHistory(accountId, userId, 0, 100);

        Map<String, Object> variables = Map.of(
            "userName", jwt.getClaimAsString("preferred_username"),
            "iban", account.getIban(),
            "balance", account.getBalance(),
            "currency", account.getCurrency(),
            "transactions", history.content()
        );

        byte[] pdfBytes = pdfService.generatePdfFromHtml("statement", variables);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=statement_" + account.getIban() + ".pdf");

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
