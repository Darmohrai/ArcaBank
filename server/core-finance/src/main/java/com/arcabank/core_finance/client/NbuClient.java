package com.arcabank.core_finance.client;

import com.arcabank.core_finance.dto.NbuRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "nbu-client", url = "${bank.integration.nbu.url}")
public interface NbuClient {

    @GetMapping("${bank.integration.nbu.json}")
    List<NbuRateResponse> getExchangeRates();
}
