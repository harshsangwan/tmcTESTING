package com.taskmanagement.project.repository;

import com.taskmanagement.project.model.entity.ProjectMember;
import com.taskmanagement.project.model.entity.ProjectMember.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    
    List<ProjectMember> findByProjectId(Long projectId);
    
    List<ProjectMember> findByUserId(Long userId);
    
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);
    
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}