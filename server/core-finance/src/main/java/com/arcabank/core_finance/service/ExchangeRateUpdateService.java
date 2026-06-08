package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.NbuClient;
import com.arcabank.core_finance.dto.NbuRateResponse;
import com.arcabank.core_finance.repository.ExchangeRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateUpdateService {

    private final NbuClient nbuClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("${bank.exchange.supported-currencies}")
    private Set<String> supportedCurrencies;

    @Value("${bank.exchange.margin}")
    private BigDecimal bankMargin;

    @PostConstruct
    public void init() {
        log.info("Initial trigger: Running exchange rate update on startup...");
        fetchAndUpdateRates();
    }

    @Scheduled(cron = "${bank.exchange.cron}")
    public void fetchAndUpdateRates() {
        log.info("Starting scheduled task: Fetching exchange rates from NBU...");

        try {
            List<NbuRateResponse> nbuRates = nbuClient.getExchangeRates();

            for (NbuRateResponse nbuRate : nbuRates) {
                if (supportedCurrencies.contains(nbuRate.cc())) {

                    BigDecimal officialRate = nbuRate.rate();

                    BigDecimal buyRate = officialRate.subtract(officialRate.multiply(bankMargin))
                        .setScale(4, RoundingMode.HALF_UP);

                    BigDecimal sellRate = officialRate.add(officialRate.multiply(bankMargin))
                        .setScale(4, RoundingMode.HALF_UP);

                    exchangeRateRepository.upsertRate(nbuRate.cc(), buyRate, sellRate);

                    log.info("Updated rate for {}: BUY={}, SELL={} (NBU={})",
                        nbuRate.cc(), buyRate, sellRate, officialRate);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch exchange rates from NBU", e);
        }
    }
}
