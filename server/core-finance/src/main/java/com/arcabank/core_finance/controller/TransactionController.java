package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.service.ExchangeService;
import com.arcabank.core_finance.service.TransactionService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Transactions", description = "API for processing internal transfers, currency exchanges, and viewing transaction history")
@RestController
@RequestMapping(RoutingRegistry.Api.Transfers.BASE)
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ExchangeService exchangeService;

    @Operation(summary = "Process internal transfer", description = "Executes a money transfer between two accounts within the bank.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer successfully processed"),
        @ApiResponse(responseCode = "400", description = "Validation error or insufficient funds"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (source account does not belong to the user)"),
        @ApiResponse(responseCode = "404", description = "Source or destination account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(RoutingRegistry.Api.Transfers.TRANSACTION)
    public ResponseEntity<Map<String, String>> makeTransfer(
        @Valid @RequestBody TransferRequest request,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        UUID transactionId = transactionService.processInternalTransfer(userId, request);

        return ResponseEntity.ok(Map.of(
            "message", "Transfer successful",
            "transactionId", transactionId.toString()
        ));
    }

    @Operation(summary = "Process currency exchange", description = "Exchanges funds between two accounts belonging to the user with different currencies, applying the current exchange rate.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange successfully processed"),
        @ApiResponse(responseCode = "400", description = "Validation error, identical currencies, or insufficient funds"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (one or both accounts do not belong to the user)"),
        @ApiResponse(responseCode = "404", description = "Source or destination account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(RoutingRegistry.Api.Transfers.EXCHANGE)
    public ResponseEntity<Map<String, String>> exchangeCurrency(
        @Valid @RequestBody ExchangeRequest request,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        UUID transactionId = exchangeService.processExchange(userId, request);

        return ResponseEntity.ok(Map.of(
            "message", "Exchange successful",
            "transactionId", transactionId.toString()
        ));
    }

    @Operation(summary = "Get all user transactions", description = "Returns a paginated history of all transactions across all accounts belonging to the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Transfers.HISTORY)
    public ResponseEntity<PageResponse<TransactionDto>> getAllTransactionsHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PageResponse<TransactionDto> response = transactionService.getAllUserTransactionHistory(userId, page, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get monthly statistics", description = "Retrieves aggregated monthly income and expense statistics across all user accounts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/stats")
    public ResponseEntity<List<MonthlyStatsDto>> getStats(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(transactionService.getMonthlyStats(userId));
    }
}
