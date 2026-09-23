package com.ojt.knowledgebase.controller;

import com.ojt.knowledgebase.dto.AddMemberRequest;
import com.ojt.knowledgebase.dto.ProjectMemberResponse;
import com.ojt.knowledgebase.service.ProjectMemberService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Project Members",
        description = "Manage project members"
)
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(
            ProjectMemberService projectMemberService) {

        this.projectMemberService = projectMemberService;
    }

    @PostMapping
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        projectMemberService.addMember(
                                projectId,
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @PathVariable Long projectId,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectMemberService.getMembers(
                        projectId,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication authentication) {

        projectMemberService.removeMember(
                projectId,
                userId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}