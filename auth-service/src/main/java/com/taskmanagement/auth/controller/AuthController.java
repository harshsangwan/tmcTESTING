package com.taskmanagement.auth.controller;

import com.taskmanagement.auth.model.dto.AuthResponse;
import com.taskmanagement.auth.model.dto.LoginRequest;
import com.taskmanagement.auth.model.dto.RegisterRequest;
import com.taskmanagement.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        AuthResponse response = authService.authenticateUser(loginRequest);
        log.info("Login successful for user: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for email: {}", registerRequest.getEmail());
        AuthResponse response = authService.registerUser(registerRequest);
        log.info("Registration successful for user: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }
}