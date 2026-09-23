package com.ojt.knowledgebase.repository;

import com.ojt.knowledgebase.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(
            Long projectId,
            Long userId
    );

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    void deleteByProjectIdAndUserId(
            Long projectId,
            Long userId
    );

    void deleteByProjectId(Long projectId);
    void deleteByUserId(Long userId);
}