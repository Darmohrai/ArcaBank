package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.ChestCreationRequest;
import com.arcabank.core_finance.dto.ChestDepositRequest;
import com.arcabank.core_finance.dto.ChestDepositResponse;
import com.arcabank.core_finance.dto.ChestResponse;
import com.arcabank.core_finance.service.ChestService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(RoutingRegistry.Api.Chests.BASE)
@RequiredArgsConstructor
public class ChestController {
    private final ChestService chestService;

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
}
