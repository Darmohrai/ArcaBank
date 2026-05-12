package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.AccountCreationRequest;
import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.dto.AccountOnlyRequest;
import com.arcabank.core_finance.dto.AccountResponse;
import com.arcabank.core_finance.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<List<AccountDto>> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userIdString = jwt.getSubject();
        UUID userId = UUID.fromString(userIdString);

        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);

        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/with-card")
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

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(
        @PathVariable("id") UUID id,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        AccountDto account = accountService.getAccountById(id, userId);

        return ResponseEntity.ok(account);
    }
}
