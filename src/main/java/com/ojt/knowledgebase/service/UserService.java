package com.ojt.knowledgebase.service;

import com.ojt.knowledgebase.dto.AdminCreateUserRequest;
import com.ojt.knowledgebase.dto.UpdateUserRequest;
import com.ojt.knowledgebase.dto.UserResponse;
import com.ojt.knowledgebase.entity.User;
import com.ojt.knowledgebase.repository.DocumentRepository;
import com.ojt.knowledgebase.repository.ProjectMemberRepository;
import com.ojt.knowledgebase.repository.ProjectRepository;
import com.ojt.knowledgebase.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            DocumentRepository documentRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    public UserResponse getUser(Long userId) {

        User user = getUserEntity(userId);

        return convertToResponse(user);
    }

    public UserResponse createUser(
            AdminCreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        User user = new User();

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setFullName(
                request.getFullName()
        );

        user.setRole(
                request.getRole()
        );

        User saved =
                userRepository.save(user);

        return convertToResponse(saved);
    }


    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request) {

        User user =
                getUserEntity(userId);


        if (!user.getEmail()
                .equalsIgnoreCase(request.getEmail())
                && userRepository
                .existsByEmail(request.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        user.setEmail(
                request.getEmail()
        );

        user.setFullName(
                request.getFullName()
        );

        user.setRole(
                request.getRole()
        );

        User saved =
                userRepository.save(user);

        return convertToResponse(saved);
    }


    @Transactional
    public void deleteUser(
            Long userId,
            String currentAdminEmail) {

        User user =
                getUserEntity(userId);


        if (user.getEmail()
                .equalsIgnoreCase(currentAdminEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot delete your own account"
            );
        }


        if (projectRepository
                .existsByOwnerId(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a user who owns projects"
            );
        }

        if (documentRepository
                .existsByUploadedById(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a user who has uploaded documents"
            );
        }


        projectMemberRepository
                .deleteByUserId(userId);

        userRepository.delete(user);
    }


    private User getUserEntity(
            Long userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );
    }

    private UserResponse convertToResponse(
            User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}