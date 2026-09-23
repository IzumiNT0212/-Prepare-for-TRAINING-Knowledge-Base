package com.ojt.knowledgebase.service;

import com.ojt.knowledgebase.dto.AddMemberRequest;
import com.ojt.knowledgebase.dto.ProjectMemberResponse;
import com.ojt.knowledgebase.entity.Project;
import com.ojt.knowledgebase.entity.ProjectMember;
import com.ojt.knowledgebase.entity.Role;
import com.ojt.knowledgebase.entity.User;
import com.ojt.knowledgebase.repository.ProjectMemberRepository;
import com.ojt.knowledgebase.repository.ProjectRepository;
import com.ojt.knowledgebase.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public ProjectMemberResponse addMember(
            Long projectId,
            String currentUserEmail,
            AddMemberRequest request) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );

        checkOwnerOrAdmin(project, currentUserEmail);

        User member = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        if (project.getOwner().getId().equals(member.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Project owner is already part of the project"
            );
        }

        if (projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        member.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User is already a member"
            );
        }

        ProjectMember projectMember =
                new ProjectMember();

        projectMember.setProject(project);
        projectMember.setUser(member);

        ProjectMember saved =
                projectMemberRepository.save(projectMember);

        return convertToResponse(saved);
    }

    public List<ProjectMemberResponse> getMembers(
            Long projectId,
            String currentUserEmail) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );

        checkOwnerOrAdmin(project, currentUserEmail);

        return projectMemberRepository
                .findByProjectId(projectId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public void removeMember(
            Long projectId,
            Long userId,
            String currentUserEmail) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );

        checkOwnerOrAdmin(project, currentUserEmail);

        if (!projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found in project"
            );
        }

        projectMemberRepository
                .deleteByProjectIdAndUserId(
                        projectId,
                        userId
                );
    }

    private void checkOwnerOrAdmin(
            Project project,
            String email) {

        User currentUser = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        boolean isOwner =
                project.getOwner()
                        .getId()
                        .equals(currentUser.getId());

        boolean isAdmin =
                currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only project owner or admin can manage members"
            );
        }
    }

    private ProjectMemberResponse convertToResponse(
            ProjectMember member) {

        return new ProjectMemberResponse(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getFullName(),
                member.getJoinedAt()
        );
    }
}