package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.service.ChestService;
import com.arcabank.core_finance.service.EscrowService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(RoutingRegistry.Api.Chests.BASE)
@RequiredArgsConstructor
public class ChestController {
    private final ChestService chestService;
    private final EscrowService escrowService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ChestResponse> createChest(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ChestCreationRequest request) {

        UUID creatorId = UUID.fromString(jwt.getSubject());

        ChestResponse response = chestService.createChest(creatorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(RoutingRegistry.Api.Chests.DEPOSIT)
    public ResponseEntity<ChestDepositResponse> depositToChest(
        @PathVariable UUID chestId,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ChestDepositRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        ChestDepositResponse response = chestService.depositToChest(userId, chestId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(RoutingRegistry.Api.Chests.VOTING)
    public ResponseEntity<MessageResponse> voteEscrow(
        @PathVariable UUID escrowId,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody VoteRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        escrowService.processVote(escrowId, userId, request);

        return ResponseEntity.ok(new MessageResponse(
            "Ваш голос успішно враховано."
        ));
    }

    @GetMapping
    public ResponseEntity<List<Chest>> getMyChests(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(chestService.getMyChests(userId));
    }

    @GetMapping("/{chestId}")
    public ResponseEntity<ChestDetailResponse> getChestDetails(
        @PathVariable UUID chestId,
        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(chestService.getChestDetails(chestId, userId));
    }

    @PostMapping(RoutingRegistry.Api.Chests.VOTES_INITIATE)
    public ResponseEntity<EscrowInitiationResponse> initiateEscrow(
        @PathVariable UUID chestId,
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody EscrowInitiationRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        EscrowInitiationResponse response = escrowService.initiateWithdrawal(chestId, userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chestId}/escrow/pending")
    public ResponseEntity<PendingEscrowResponse> getPendingEscrow(
        @PathVariable UUID chestId,
        @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        PendingEscrowResponse response = escrowService.getPendingEscrow(chestId, userId);

        return ResponseEntity.ok(response);
    }
}
