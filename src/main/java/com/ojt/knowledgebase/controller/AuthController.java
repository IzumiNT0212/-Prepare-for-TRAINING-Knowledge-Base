package com.ojt.knowledgebase.controller;

import com.ojt.knowledgebase.dto.AuthResponse;
import com.ojt.knowledgebase.dto.LoginRequest;
import com.ojt.knowledgebase.dto.RegisterRequest;
import com.ojt.knowledgebase.entity.User;
import com.ojt.knowledgebase.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Register and login APIs"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        User user =
                authService.register(request);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request) {

        AuthResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}