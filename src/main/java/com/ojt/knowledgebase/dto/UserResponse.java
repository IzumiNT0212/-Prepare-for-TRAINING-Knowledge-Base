package com.ojt.knowledgebase.dto;

import com.ojt.knowledgebase.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
}