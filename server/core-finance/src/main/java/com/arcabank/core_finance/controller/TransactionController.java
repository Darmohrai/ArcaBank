package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.ExchangeRequest;
import com.arcabank.core_finance.dto.TransferRequest;
import com.arcabank.core_finance.service.ExchangeService;
import com.arcabank.core_finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ExchangeService exchangeService;

    @PostMapping("/transaction")
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

    @PostMapping("/exchange")
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
}
