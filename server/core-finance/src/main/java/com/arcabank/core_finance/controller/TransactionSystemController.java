package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.DepositRequest;
import com.arcabank.core_finance.service.TransactionSystemService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "System Transactions", description = "API for handling external financial operations such as account deposits")
@Slf4j
@RestController
@RequestMapping(RoutingRegistry.Api.Transactions.BASE)
@RequiredArgsConstructor
public class TransactionSystemController {

    private final TransactionSystemService transactionSystemService;

    @Operation(summary = "Deposit funds", description = "Processes an external deposit to add funds to a specific user account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successfully processed"),
        @ApiResponse(responseCode = "400", description = "Validation error or invalid deposit amount"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (account belongs to another user)"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(RoutingRegistry.Api.Transactions.DEPOSIT)
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
