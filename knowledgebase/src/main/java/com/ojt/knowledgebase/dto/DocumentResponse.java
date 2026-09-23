package com.ojt.knowledgebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String fileName;
    private String originalName;
    private String fileType;
    private Long fileSize;

    private Long projectId;

    private Long uploadedById;
    private String uploadedByEmail;

    private LocalDateTime createdAt;
}