package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.service.ExchangeService;
import com.arcabank.core_finance.service.TransactionService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(RoutingRegistry.Api.Transfers.BASE)
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ExchangeService exchangeService;

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

    @GetMapping(RoutingRegistry.Api.Transfers.HISTORY)
    public ResponseEntity<PageResponse<TransactionDto>> getAllTransactionsHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        PageResponse<TransactionDto> response = transactionService.getAllUserTransactionHistory(userId, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<MonthlyStatsDto>> getStats(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(transactionService.getMonthlyStats(userId));
    }
}
