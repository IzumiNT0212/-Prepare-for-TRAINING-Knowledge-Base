package com.ojt.knowledgebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectMemberResponse {

    private Long userId;
    private String email;
    private String fullName;
    private LocalDateTime joinedAt;
}