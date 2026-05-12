package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.model.ExchangeRate;
import com.arcabank.core_finance.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateRepository repository;

    @GetMapping
    public ResponseEntity<List<ExchangeRate>> getRates() {
        return ResponseEntity.ok(repository.findAll());
    }
}
