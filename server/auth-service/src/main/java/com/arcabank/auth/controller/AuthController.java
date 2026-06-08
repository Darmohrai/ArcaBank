package com.arcabank.auth.controller;

import com.arcabank.auth.dto.LoginRequest;
import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.dto.TokenResponse;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.service.UserLoginService;
import com.arcabank.auth.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/public")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService registrationService;
    private final UserLoginService loginService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationRequest request) {
        registrationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @PostMapping("/login")
    public TokenResponse loginUser(@RequestBody LoginRequest request) {
        return loginService.authenticate(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException("Refresh token is required", "400", HttpStatus.BAD_REQUEST );
        }

        TokenResponse response = loginService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
