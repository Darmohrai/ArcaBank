package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.dto.MonthlyGoalProgressResponse;
import com.arcabank.core_finance.dto.MonthlyGoalRequest;
import com.arcabank.core_finance.service.MonthlyGoalService;
import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Monthly Goals", description = "API for managing monthly financial goals and tracking progress")
@RestController
@RequestMapping(RoutingRegistry.Api.Goals.BASE)
@RequiredArgsConstructor
public class MonthlyGoalController {

    private final MonthlyGoalService monthlyGoalService;

    @Operation(
        summary = "Create or update a monthly goal",
        description = "Sets a new financial goal or updates an existing one for the specified month and year. Returns the current progress immediately."
    )
    @PostMapping
    public ResponseEntity<MonthlyGoalProgressResponse> setGoal(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody MonthlyGoalRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MonthlyGoalProgressResponse response = monthlyGoalService.setMonthlyGoal(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get monthly goal progress",
        description = "Returns income/expense statistics, net income, and the remaining amount needed to reach the goal for the selected month."
    )
    @GetMapping(RoutingRegistry.Api.Goals.PROGRESS)
    public ResponseEntity<MonthlyGoalProgressResponse> getProgress(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable int year,
        @PathVariable int month) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MonthlyGoalProgressResponse response = monthlyGoalService.getGoalProgress(userId, year, month);

        return ResponseEntity.ok(response);
    }
}
