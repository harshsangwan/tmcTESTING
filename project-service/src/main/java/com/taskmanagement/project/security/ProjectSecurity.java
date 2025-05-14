package com.taskmanagement.project.security;

import com.taskmanagement.project.model.entity.ProjectMember;
import com.taskmanagement.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectSecurity {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     * Check if the current user is the creator of the project
     */
    public boolean isProjectCreator(Long projectId, Long createdBy) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        return userPrincipal.getId().equals(createdBy);
    }
    
    /**
     * Check if the current user is a member of the project
     */
    public boolean isProjectMember(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userPrincipal.getId());
    }
    
    /**
     * Check if the current user is a manager or owner of the project
     */
    public boolean isProjectManager(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        // Admin users have manager privileges
        if (userPrincipal.isAdmin()) {
            return true;
        }
        
        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userPrincipal.getId());
        return projectMember.isPresent() && 
               (projectMember.get().getRole() == ProjectMember.ProjectRole.OWNER || 
                projectMember.get().getRole() == ProjectMember.ProjectRole.MANAGER);
    }
    
    /**
     * Check if the current user is the owner of the project
     */
    public boolean isProjectOwner(Long projectId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        // Admin users have owner privileges
        if (userPrincipal.isAdmin()) {
            return true;
        }
        
        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userPrincipal.getId());
        return projectMember.isPresent() && projectMember.get().getRole() == ProjectMember.ProjectRole.OWNER;
    }
    
    /**
     * Get the current authenticated user
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        
        return (UserPrincipal) authentication.getPrincipal();
    }
}