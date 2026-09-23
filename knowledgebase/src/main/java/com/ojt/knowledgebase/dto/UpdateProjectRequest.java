package com.ojt.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;
}