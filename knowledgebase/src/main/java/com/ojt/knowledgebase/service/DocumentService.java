package com.ojt.knowledgebase.service;

import com.ojt.knowledgebase.dto.DocumentResponse;
import com.ojt.knowledgebase.entity.Document;
import com.ojt.knowledgebase.entity.Project;
import com.ojt.knowledgebase.entity.Role;
import com.ojt.knowledgebase.entity.User;
import com.ojt.knowledgebase.repository.DocumentRepository;
import com.ojt.knowledgebase.repository.ProjectMemberRepository;
import com.ojt.knowledgebase.repository.ProjectRepository;
import com.ojt.knowledgebase.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "doc", "docx",
                    "xls", "xlsx",
                    "ppt", "pptx",
                    "md", "txt",
                    "jpg", "jpeg",
                    "png", "gif",
                    "svg", "bmp",
                    "mp4", "mov", "avi"
            );

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final MinioStorageService minioStorageService;

    public DocumentService(
            DocumentRepository documentRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            MinioStorageService minioStorageService) {

        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.minioStorageService = minioStorageService;
    }



    public DocumentResponse upload(
            Long projectId,
            String currentUserEmail,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File is empty"
            );
        }

        String extension =
                validateAndGetExtension(file);

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );

        User user = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        checkProjectAccess(project, user);

        String originalName =
                file.getOriginalFilename();

        String generatedName =
                UUID.randomUUID()
                        + "."
                        + extension;

        String objectName =
                "projects/"
                        + projectId
                        + "/"
                        + generatedName;


        try {

            minioStorageService.upload(
                    objectName,
                    file
            );

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload file to MinIO"
            );
        }

        Document document = new Document();

        document.setFileName(generatedName);
        document.setOriginalName(originalName);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStoragePath(objectName);
        document.setProject(project);
        document.setUploadedBy(user);

        try {

            Document saved =
                    documentRepository.save(document);

            return convertToResponse(saved);

        } catch (Exception e) {

            try {
                minioStorageService.delete(objectName);
            } catch (Exception ignored) {
            }

            throw e;
        }
    }


    // LIST DOCUMENTS


    public List<DocumentResponse> getDocuments(
            Long projectId,
            String currentUserEmail) {

        checkAccess(
                projectId,
                currentUserEmail
        );

        return documentRepository
                .findByProjectId(projectId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public byte[] download(
            Long documentId,
            String currentUserEmail) {

        Document document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Document not found"
                                )
                        );

        checkAccess(
                document.getProject().getId(),
                currentUserEmail
        );

        try (
                InputStream inputStream =
                        minioStorageService.download(
                                document.getStoragePath()
                        )
        ) {

            return inputStream.readAllBytes();

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not download file from MinIO"
            );
        }
    }



    public void delete(
            Long documentId,
            String currentUserEmail) {

        Document document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Document not found"
                                )
                        );

        checkAccess(
                document.getProject().getId(),
                currentUserEmail
        );

        try {

            minioStorageService.delete(
                    document.getStoragePath()
            );

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not delete file from MinIO"
            );
        }

        documentRepository.delete(document);
    }


    public Document getDocument(Long documentId) {

        return documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Document not found"
                        )
                );
    }

    private void checkAccess(
            Long projectId,
            String email) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        checkProjectAccess(project, user);
    }

    private void checkProjectAccess(
            Project project,
            User user) {

        boolean isOwner =
                project.getOwner()
                        .getId()
                        .equals(user.getId());

        boolean isMember =
                projectMemberRepository
                        .existsByProjectIdAndUserId(
                                project.getId(),
                                user.getId()
                        );

        boolean isAdmin =
                user.getRole() == Role.ADMIN;

        if (!isOwner
                && !isMember
                && !isAdmin) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this project"
            );
        }
    }


    private String validateAndGetExtension(
            MultipartFile file) {

        String originalName =
                file.getOriginalFilename();

        if (originalName == null
                || originalName.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        int dotIndex =
                originalName.lastIndexOf(".");

        if (dotIndex <= 0
                || dotIndex
                == originalName.length() - 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have an extension"
            );
        }

        String extension =
                originalName
                        .substring(dotIndex + 1)
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS
                .contains(extension)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File type is not allowed"
            );
        }

        return extension;
    }

    private DocumentResponse convertToResponse(
            Document document) {

        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getOriginalName(),
                document.getFileType(),
                document.getFileSize(),
                document.getProject().getId(),
                document.getUploadedBy().getId(),
                document.getUploadedBy().getEmail(),
                document.getCreatedAt()
        );
    }
}