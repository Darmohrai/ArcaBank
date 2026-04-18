package com.arcabank.auth.service;

import com.arcabank.auth.dto.LoginRequest;
import com.arcabank.auth.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class UserLoginService {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final RestClient restClient = RestClient.create();

    public TokenResponse authenticate(LoginRequest request) {
        log.info("Knight {} is requesting the keys to the Citadel...", request.username());

        String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", "arcabank-frontend");
        formData.add("username", request.username());
        formData.add("password", request.password());

        try {
            TokenResponse response = restClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(TokenResponse.class);

            log.info("Access granted for knight {}.", request.username());
            return response;

        } catch (Exception e) {
            log.error("Failed to authenticate knight {}. Invalid credentials?", request.username());
            throw new RuntimeException("Invalid username or password");
        }
    }
}
