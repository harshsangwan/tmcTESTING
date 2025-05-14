package com.taskmanagement.project.service;

import com.taskmanagement.project.client.AuthServiceClient;
import com.taskmanagement.project.exception.AccessDeniedException;
import com.taskmanagement.project.exception.ResourceNotFoundException;
import com.taskmanagement.project.model.dto.ProjectMemberDto;
import com.taskmanagement.project.model.dto.UserDto;
import com.taskmanagement.project.model.entity.Project;
import com.taskmanagement.project.model.entity.ProjectMember;
import com.taskmanagement.project.repository.ProjectMemberRepository;
import com.taskmanagement.project.repository.ProjectRepository;
import com.taskmanagement.project.security.ProjectSecurity;
import com.taskmanagement.project.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectSecurity projectSecurity;
    private final AuthServiceClient authServiceClient;

    /**
     * Get all members of a project
     */
    public List<ProjectMemberDto> getProjectMembers(Long projectId) {
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the user is a member of this project
        if (!projectSecurity.isProjectMember(projectId) && !projectSecurity.getCurrentUser().isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this project's members");
        }
        
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Add a member to a project
     */
    @Transactional
    public ProjectMemberDto addProjectMember(Long projectId, ProjectMemberDto memberDto) {
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the user has permission to add members
        if (!projectSecurity.isProjectManager(projectId)) {
            throw new AccessDeniedException("You don't have permission to add members to this project");
        }
        
        // Check if the user is already a member
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, memberDto.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }
        
        // Verify the user exists by calling the auth service
        UserDto user;
        try {
            String authHeader = "Bearer " + projectSecurity.getCurrentUser().getToken();
            user = authServiceClient.getUserById(authHeader, memberDto.getUserId());
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with id: " + memberDto.getUserId());
        }
        
        // Create project member
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .role(memberDto.getRole())
                .build();
        
        ProjectMember savedMember = projectMemberRepository.save(member);
        log.info("Added user {} to project {}", user.getName(), project.getName());
        
        return mapToDto(savedMember);
    }
    
    /**
     * Update a project member's role
     */
    @Transactional
    public ProjectMemberDto updateProjectMemberRole(Long projectId, Long userId, ProjectMember.ProjectRole role) {
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the user has permission to update member roles
        if (!projectSecurity.isProjectOwner(projectId)) {
            throw new AccessDeniedException("You don't have permission to update member roles in this project");
        }
        
        // Check if the user is a member
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));
        
        // Don't allow changing the role of the owner
        if (member.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new IllegalArgumentException("Cannot change the role of the project owner");
        }
        
        // Update role
        member.setRole(role);
        
        ProjectMember updatedMember = projectMemberRepository.save(member);
        log.info("Updated role of user {} in project {} to {}", member.getUserName(), project.getName(), role);
        
        return mapToDto(updatedMember);
    }
    
    /**
     * Remove a member from a project
     */
    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the user has permission to remove members
        if (!projectSecurity.isProjectManager(projectId)) {
            throw new AccessDeniedException("You don't have permission to remove members from this project");
        }
        
        // Check if the user is a member
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));
        
        // Don't allow removing the owner
        if (member.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the project owner");
        }
        
        // Remove the member
        projectMemberRepository.delete(member);
        log.info("Removed user {} from project {}", member.getUserName(), project.getName());
    }
    
    /**
     * Leave a project
     */
    @Transactional
    public void leaveProject(Long projectId) {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the user is a member
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this project"));
        
        // Don't allow the owner to leave
        if (member.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new IllegalArgumentException("Project owner cannot leave the project. Transfer ownership first.");
        }
        
        // Remove the member
        projectMemberRepository.delete(member);
        log.info("User {} left project {}", currentUser.getName(), project.getName());
    }
    
    /**
     * Transfer project ownership
     */
    @Transactional
    public void transferProjectOwnership(Long projectId, Long newOwnerId) {
        // Check if the project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        // Check if the current user is the owner
        if (!projectSecurity.isProjectOwner(projectId)) {
            throw new AccessDeniedException("Only the project owner can transfer ownership");
        }
        
        // Check if the new owner is a member
        ProjectMember newOwnerMember = projectMemberRepository.findByProjectIdAndUserId(projectId, newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("New owner is not a member of this project"));
        
        // Get current owner
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        ProjectMember currentOwnerMember = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this project"));
        
        // Transfer ownership
        currentOwnerMember.setRole(ProjectMember.ProjectRole.MANAGER);
        newOwnerMember.setRole(ProjectMember.ProjectRole.OWNER);
        
        projectMemberRepository.save(currentOwnerMember);
        projectMemberRepository.save(newOwnerMember);
        
        log.info("Transferred ownership of project {} from {} to {}", 
                project.getName(), currentUser.getName(), newOwnerMember.getUserName());
    }
    
    /**
     * Map ProjectMember entity to ProjectMemberDto
     */
    private ProjectMemberDto mapToDto(ProjectMember member) {
        return ProjectMemberDto.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .userId(member.getUserId())
                .userName(member.getUserName())
                .userEmail(member.getUserEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}