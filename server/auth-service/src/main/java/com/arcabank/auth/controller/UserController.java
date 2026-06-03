package com.arcabank.auth.controller;

import com.arcabank.auth.dto.UserPhoneResponse;
import com.arcabank.auth.dto.UserResponse;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.model.User;
import com.arcabank.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        UserResponse response = new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassportId(),
            user.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phone_number}")
    public ResponseEntity<UserPhoneResponse> getUserByPhone(@PathVariable("phone_number") String phone) {

        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new AppException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        UserPhoneResponse response = new UserPhoneResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }
}
