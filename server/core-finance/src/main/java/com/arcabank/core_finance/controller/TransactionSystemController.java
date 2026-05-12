package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.DepositRequest;
import com.arcabank.core_finance.service.TransactionSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionSystemController {

    private final TransactionSystemService transactionSystemService;

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
        @Valid @RequestBody DepositRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();

        log.info("REST request to deposit funds. User: {}, Account: {}, Amount: {}",
            userId, request.accountId(), request.amount());

        transactionSystemService.processDeposit(request, userId);

        return ResponseEntity.ok("Deposit of " + request.amount() + " processed successfully.");
    }
}
