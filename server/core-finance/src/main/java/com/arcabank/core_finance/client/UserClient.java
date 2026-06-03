package com.arcabank.core_finance.client;

import com.arcabank.core_finance.dto.UserPhoneResponse;
import com.arcabank.core_finance.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${bank.integration.auth-service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("api/v1/users/{id}")
    UserResponse getUserById(@PathVariable("id") UUID id);

    @GetMapping("api/v1/users/phone/{phone}")
    UserPhoneResponse getUserByPhone(@PathVariable("phone") String phone);
}
