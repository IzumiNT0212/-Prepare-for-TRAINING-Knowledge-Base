package com.ojt.knowledgebase.controller;

import com.ojt.knowledgebase.dto.DocumentResponse;
import com.ojt.knowledgebase.entity.Document;
import com.ojt.knowledgebase.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController

@Tag(
        name = "Documents",
        description = "Upload, download and manage project documents"
)

@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;


    public DocumentController(
            DocumentService documentService) {

        this.documentService = documentService;
    }


    @Operation(
            summary = "Upload document",
            description = """
                    Upload a document to a project.

                    The authenticated user must be:
                    - Project Owner
                    - Project Member
                    - ADMIN

                    Supported file types include:
                    PDF, Word, Excel, PowerPoint,
                    Markdown, TXT, images and videos.

                    The physical file is stored in MinIO.
                    Metadata is stored in PostgreSQL.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "201",
                    description = "Document uploaded successfully"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or unsupported file"
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
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Could not upload file to MinIO"
            )

    })

    @PostMapping(
            value = "/api/projects/{projectId}/documents",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<DocumentResponse> upload(

            @PathVariable
            Long projectId,

            @RequestParam("file")
            MultipartFile file,

            Authentication authentication) {

        DocumentResponse response =
                documentService.upload(
                        projectId,
                        authentication.getName(),
                        file
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }



    @Operation(
            summary = "Get project documents",
            description = """
                    Return all documents belonging to a project.

                    The authenticated user must have access
                    to the project.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Documents retrieved successfully"
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

    @GetMapping(
            "/api/projects/{projectId}/documents"
    )
    public ResponseEntity<List<DocumentResponse>>
    getDocuments(

            @PathVariable
            Long projectId,

            Authentication authentication) {

        List<DocumentResponse> documents =
                documentService.getDocuments(
                        projectId,
                        authentication.getName()
                );

        return ResponseEntity.ok(documents);
    }


    @Operation(
            summary = "Download document",
            description = """
                    Download a document from MinIO.

                    The authenticated user must have access
                    to the project containing the document.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Document downloaded successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found"
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Could not download file from MinIO"
            )

    })

    @GetMapping(
            "/api/documents/{documentId}/download"
    )
    public ResponseEntity<byte[]> download(

            @PathVariable
            Long documentId,

            Authentication authentication) {

        Document document =
                documentService.getDocument(
                        documentId
                );

        byte[] file =
                documentService.download(
                        documentId,
                        authentication.getName()
                );

        return ResponseEntity
                .ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                document.getOriginalName()
                                + "\""
                )

                .header(
                        HttpHeaders.CONTENT_TYPE,

                        document.getFileType() != null
                                ? document.getFileType()
                                : "application/octet-stream"
                )

                .body(file);
    }


    @Operation(
            summary = "Delete document",
            description = """
                    Delete a document.

                    The file will be deleted from MinIO
                    and its metadata will be deleted
                    from PostgreSQL.
                    """
    )

    @ApiResponses({

            @ApiResponse(
                    responseCode = "204",
                    description = "Document deleted successfully"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Document not found"
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Could not delete file from MinIO"
            )

    })

    @DeleteMapping(
            "/api/documents/{documentId}"
    )
    public ResponseEntity<Void> delete(

            @PathVariable
            Long documentId,

            Authentication authentication) {

        documentService.delete(
                documentId,
                authentication.getName()
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}