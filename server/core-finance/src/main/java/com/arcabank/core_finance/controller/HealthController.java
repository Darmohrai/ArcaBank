package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.utils.RoutingRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

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
