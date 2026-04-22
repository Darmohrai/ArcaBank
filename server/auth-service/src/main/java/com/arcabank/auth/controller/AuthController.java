package com.arcabank.auth.controller;

import com.arcabank.auth.dto.LoginRequest;
import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.dto.TokenResponse;
import com.arcabank.auth.service.UserLoginService;
import com.arcabank.auth.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
