package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.CardDto;
import com.arcabank.core_finance.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final AccountService accountService;

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCardById(
        @PathVariable("cardId") UUID cardId,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        CardDto card = accountService.getCardById(cardId, userId);

        return ResponseEntity.ok(card);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardDto>> getAllMyCards(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<CardDto> cards = accountService.getAllCardsByUserId(userId);

        return ResponseEntity.ok(cards);
    }
}
