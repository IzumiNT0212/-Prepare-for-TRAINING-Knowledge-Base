package com.ojt.knowledgebase.controller;

import com.ojt.knowledgebase.dto.CreateProjectRequest;
import com.ojt.knowledgebase.dto.ProjectResponse;
import com.ojt.knowledgebase.dto.UpdateProjectRequest;
import com.ojt.knowledgebase.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")

@Tag(
        name = "Projects",
        description = "Project management APIs"
)

@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;


    public ProjectController(
            ProjectService projectService) {

        this.projectService = projectService;
    }


    @Operation(
            summary = "Create project",
            description = """
                    Create a new project.

                    Only users with OWNER or ADMIN role
                    are allowed to create projects.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project data"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Only OWNER or ADMIN can create projects"
            )

    })

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(

            @Valid
            @RequestBody
            CreateProjectRequest request,

            Authentication authentication) {

        ProjectResponse response =
                projectService.createProject(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Get my projects",
            description = """
                    Return all projects related to
                    the currently authenticated user.

                    Includes:
                    - Projects owned by the user
                    - Projects where the user is a member
                    - All projects if the user is ADMIN
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )

    })

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>>
    getMyProjects(
            Authentication authentication) {

        List<ProjectResponse> projects =
                projectService.getMyProjects(
                        authentication.getName()
                );

        return ResponseEntity.ok(projects);
    }


    @Operation(
            summary = "Get project by ID",
            description = """
                    Get detailed information about a project.

                    Access is allowed when the current user is:
                    - Project Owner
                    - Project Member
                    - ADMIN
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "User cannot access this project"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )

    })

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(

            @PathVariable
            Long projectId,

            Authentication authentication) {

        ProjectResponse response =
                projectService.getProject(
                        projectId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Update project",
            description = """
                    Update project name and description.

                    Only the project OWNER or ADMIN
                    can update the project.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project data"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Only project OWNER or ADMIN can update project"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )

    })

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(

            @PathVariable
            Long projectId,

            @Valid
            @RequestBody
            UpdateProjectRequest request,

            Authentication authentication) {

        ProjectResponse response =
                projectService.updateProject(
                        projectId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Delete project",
            description = """
                    Delete a project.

                    Only the project OWNER or ADMIN
                    can delete the project.

                    Related project members,
                    document metadata and files stored
                    in MinIO will also be removed.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "204",
                    description = "Project deleted successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Only project OWNER or ADMIN can delete project"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )

    })

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(

            @PathVariable
            Long projectId,

            Authentication authentication) {

        projectService.deleteProject(
                projectId,
                authentication.getName()
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}