package com.ojt.knowledgebase.controller;

import com.ojt.knowledgebase.dto.AdminCreateUserRequest;
import com.ojt.knowledgebase.dto.UpdateUserRequest;
import com.ojt.knowledgebase.dto.UserResponse;
import com.ojt.knowledgebase.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Users - Admin",
        description = "Admin user management APIs"
)
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService) {

        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>>
    getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUser(userId)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid
            @RequestBody
            AdminCreateUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.createUser(request)
                );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid
            @RequestBody
            UpdateUserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(
                        userId,
                        request
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {

        userService.deleteUser(
                userId,
                authentication.getName()
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}