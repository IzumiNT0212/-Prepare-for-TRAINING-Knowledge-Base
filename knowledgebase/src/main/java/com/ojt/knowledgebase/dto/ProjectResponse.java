package com.ojt.knowledgebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;

    private Long ownerId;
    private String ownerEmail;

    private LocalDateTime createdAt;
}