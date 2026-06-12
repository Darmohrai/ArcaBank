package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System Health", description = "API for system diagnostics and health monitoring")
@RestController
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "System health check", description = "Verifies the application's operational status and checks the database connectivity.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System is healthy and the database is reachable"),
        @ApiResponse(responseCode = "503", description = "Service is unavailable (e.g., database connection failure)")
    })
    @GetMapping(RoutingRegistry.Api.System.HEALTH)
    public ResponseEntity<String> healthCheck() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Database is unavailable");
        }
    }
}
