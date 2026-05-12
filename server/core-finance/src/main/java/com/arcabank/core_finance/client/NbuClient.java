package com.arcabank.core_finance.client;

import com.arcabank.core_finance.dto.NbuRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "nbu-client", url = "https://bank.gov.ua/NBUStatService/v1/statdirectory")
public interface NbuClient {

    @GetMapping("/exchange?json")
    List<NbuRateResponse> getExchangeRates();
}
