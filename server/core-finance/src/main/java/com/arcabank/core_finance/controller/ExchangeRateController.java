package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.model.ExchangeRate;
import com.arcabank.core_finance.repository.ExchangeRateRepository;
import com.arcabank.core_finance.utils.RoutingRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Exchange Rates", description = "API for retrieving current currency exchange rates")
@RestController
@RequestMapping(RoutingRegistry.Api.ExchangeRates.BASE)
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateRepository repository;

    @Operation(summary = "Get all exchange rates", description = "Returns a list of all current currency exchange rates available in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of exchange rates"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ExchangeRate>> getRates() {
        return ResponseEntity.ok(repository.findAll());
    }
}
