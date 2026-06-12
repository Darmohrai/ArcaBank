package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.CardCreationRequest;
import com.arcabank.core_finance.dto.CardDto;
import com.arcabank.core_finance.service.AccountService;
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

import java.util.Map;
import java.util.UUID;
import java.util.List;

@Tag(name = "Cards", description = "API for managing physical and virtual bank cards")
@RestController
@RequestMapping(RoutingRegistry.Api.Cards.BASE)
@RequiredArgsConstructor
public class CardController {
    private final AccountService accountService;

    @Operation(summary = "Get card details", description = "Returns detailed information about a specific card by its ID (only if it belongs to the authenticated user).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card successfully found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (card belongs to another user)"),
        @ApiResponse(responseCode = "404", description = "Card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Cards.BY_ID)
    public ResponseEntity<CardDto> getCardById(
        @PathVariable("cardId") UUID cardId,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        CardDto card = accountService.getCardById(cardId, userId);

        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Get all user cards", description = "Returns a list of all active and blocked cards belonging to the currently authenticated user across all their accounts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of cards"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(RoutingRegistry.Api.Cards.ALL)
    public ResponseEntity<List<CardDto>> getAllMyCards(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<CardDto> cards = accountService.getAllCardsByUserId(userId);

        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Issue a new card", description = "Issues a new physical or virtual card linked to an existing account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Card successfully issued"),
        @ApiResponse(responseCode = "400", description = "Validation error of input data or account restrictions"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied (account belongs to another user)"),
        @ApiResponse(responseCode = "404", description = "Target account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error (e.g., PAN generation failure)")
    })
    @PostMapping(RoutingRegistry.Api.Cards.CREATE)
    public ResponseEntity<CardDto> createCard(
        @PathVariable UUID accountId,
        @Valid @RequestBody CardCreationRequest request,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CardDto card = accountService.issueCardForAccount(userId, accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @Operation(summary = "Block card", description = "Temporarily blocks an active card to prevent further transactions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card successfully blocked"),
        @ApiResponse(responseCode = "400", description = "Card is already blocked"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(RoutingRegistry.Api.Cards.BLOCK)
    public ResponseEntity<Map<String, String>> blockCard(
        @PathVariable("cardId") UUID cardId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.blockCard(cardId, userId);

        return ResponseEntity.ok(Map.of("message", "Card successfully blocked"));
    }

    @Operation(summary = "Unblock card", description = "Removes the block from a previously blocked card, restoring its ability to perform transactions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card successfully unblocked"),
        @ApiResponse(responseCode = "400", description = "Card is already active"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Card not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(RoutingRegistry.Api.Cards.UNBLOCK)
    public ResponseEntity<Map<String, String>> unblockCard(
        @PathVariable("cardId") UUID cardId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.unblockCard(cardId, userId);

        return ResponseEntity.ok(Map.of("message", "Card successfully unblocked"));
    }
}
