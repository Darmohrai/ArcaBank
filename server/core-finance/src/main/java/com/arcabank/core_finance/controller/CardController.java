package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.CardCreationRequest;
import com.arcabank.core_finance.dto.CardDto;
import com.arcabank.core_finance.service.AccountService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping(RoutingRegistry.Api.Cards.BASE)
@RequiredArgsConstructor
public class CardController {
    private final AccountService accountService;

    @GetMapping(RoutingRegistry.Api.Cards.BY_ID)
    public ResponseEntity<CardDto> getCardById(
            @PathVariable("cardId") UUID cardId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        CardDto card = accountService.getCardById(cardId, userId);

        return ResponseEntity.ok(card);
    }

    @GetMapping(RoutingRegistry.Api.Cards.ALL)
    public ResponseEntity<List<CardDto>> getAllMyCards(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<CardDto> cards = accountService.getAllCardsByUserId(userId);

        return ResponseEntity.ok(cards);
    }

    @PostMapping(RoutingRegistry.Api.Cards.CREATE)
    public ResponseEntity<CardDto> createCard(
            @PathVariable UUID accountId,
            @Valid @RequestBody CardCreationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CardDto card = accountService.issueCardForAccount(userId, accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }
}
