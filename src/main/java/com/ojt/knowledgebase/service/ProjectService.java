package com.ojt.knowledgebase.service;

import com.ojt.knowledgebase.dto.CreateProjectRequest;
import com.ojt.knowledgebase.dto.ProjectResponse;
import com.ojt.knowledgebase.dto.UpdateProjectRequest;

import com.ojt.knowledgebase.entity.Document;
import com.ojt.knowledgebase.entity.Project;
import com.ojt.knowledgebase.entity.ProjectMember;
import com.ojt.knowledgebase.entity.Role;
import com.ojt.knowledgebase.entity.User;

import com.ojt.knowledgebase.repository.DocumentRepository;
import com.ojt.knowledgebase.repository.ProjectMemberRepository;
import com.ojt.knowledgebase.repository.ProjectRepository;
import com.ojt.knowledgebase.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final DocumentRepository documentRepository;
    private final MinioStorageService minioStorageService;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            DocumentRepository documentRepository,
            MinioStorageService minioStorageService) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.documentRepository = documentRepository;
        this.minioStorageService = minioStorageService;
    }




    public ProjectResponse createProject(
            String email,
            CreateProjectRequest request) {

        User user = getUser(email);

        if (user.getRole() != Role.OWNER
                && user.getRole() != Role.ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only OWNER or ADMIN can create projects"
            );
        }

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(user);

        Project saved =
                projectRepository.save(project);

        return convertToResponse(saved);
    }



    public ProjectResponse getProject(
            Long projectId,
            String email) {

        Project project = getProjectEntity(projectId);

        User user = getUser(email);

        checkProjectAccess(project, user);

        return convertToResponse(project);
    }




    public List<ProjectResponse> getMyProjects(
            String email) {

        User user = getUser(email);


        Map<Long, Project> projects =
                new LinkedHashMap<>();



        projectRepository
                .findByOwnerId(user.getId())
                .forEach(project ->
                        projects.put(
                                project.getId(),
                                project
                        )
                );



        projectMemberRepository
                .findByUserId(user.getId())
                .stream()
                .map(ProjectMember::getProject)
                .forEach(project ->
                        projects.put(
                                project.getId(),
                                project
                        )
                );



        if (user.getRole() == Role.ADMIN) {

            projectRepository
                    .findAll()
                    .forEach(project ->
                            projects.put(
                                    project.getId(),
                                    project
                            )
                    );
        }


        return projects
                .values()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }




    public ProjectResponse updateProject(
            Long projectId,
            String email,
            UpdateProjectRequest request) {

        Project project =
                getProjectEntity(projectId);

        User user =
                getUser(email);

        checkOwnerOrAdmin(
                project,
                user
        );

        project.setName(
                request.getName()
        );

        project.setDescription(
                request.getDescription()
        );

        Project saved =
                projectRepository.save(project);

        return convertToResponse(saved);
    }



    @Transactional
    public void deleteProject(
            Long projectId,
            String email) {

        Project project =
                getProjectEntity(projectId);

        User user =
                getUser(email);

        checkOwnerOrAdmin(
                project,
                user
        );



        List<Document> documents =
                documentRepository
                        .findByProjectId(projectId);

        for (Document document : documents) {

            try {

                minioStorageService.delete(
                        document.getStoragePath()
                );

            } catch (Exception e) {

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not delete project files from MinIO"
                );
            }
        }



        documentRepository.deleteAll(
                documents
        );



        projectMemberRepository
                .deleteByProjectId(projectId);



        projectRepository.delete(project);
    }



    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
    }


    private Project getProjectEntity(
            Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );
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
                user.getRole()
                        == Role.ADMIN;

        if (!isOwner
                && !isMember
                && !isAdmin) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this project"
            );
        }
    }


    private void checkOwnerOrAdmin(
            Project project,
            User user) {

        boolean isOwner =
                project.getOwner()
                        .getId()
                        .equals(user.getId());

        boolean isAdmin =
                user.getRole()
                        == Role.ADMIN;

        if (!isOwner && !isAdmin) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only project owner or admin can modify this project"
            );
        }
    }


    private ProjectResponse convertToResponse(
            Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getOwner().getEmail(),
                project.getCreatedAt()
        );
    }
}