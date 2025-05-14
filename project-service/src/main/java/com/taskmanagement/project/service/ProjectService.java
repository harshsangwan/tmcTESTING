package com.taskmanagement.project.service;

import com.taskmanagement.project.client.AuthServiceClient;
import com.taskmanagement.project.exception.AccessDeniedException;
import com.taskmanagement.project.exception.ResourceNotFoundException;
import com.taskmanagement.project.model.dto.ProjectDto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectSecurity projectSecurity;
    private final AuthServiceClient authServiceClient;

    /**
     * Get all projects based on user role and permissions
     */
    public List<ProjectDto> getAllProjects() {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        List<Project> projects;
        
        if (currentUser.isAdmin()) {
            // Admin users can see all projects
            projects = projectRepository.findAll();
        } else {
            // Other users can only see projects they are members of
            projects = projectRepository.findByMemberId(currentUser.getId());
        }
        
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get project by ID
     */
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Check if the user is a member of this project
        if (!projectSecurity.isProjectMember(id) && !projectSecurity.getCurrentUser().isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view this project");
        }
        
        return mapToDto(project);
    }
    
    /**
     * Create a new project
     */
    @Transactional
    public ProjectDto createProject(ProjectDto projectDto) {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        // Create the project
        Project project = Project.builder()
                .name(projectDto.getName())
                .description(projectDto.getDescription())
                .startDate(projectDto.getStartDate())
                .endDate(projectDto.getEndDate())
                .progress(projectDto.getProgress() != null ? projectDto.getProgress() : 0)
                .status(projectDto.getStatus() != null ? projectDto.getStatus() : Project.ProjectStatus.NOT_STARTED)
                .tasksCount(0)
                .createdBy(currentUser.getId())
                .createdByName(currentUser.getName())
                .build();
        
        Project savedProject = projectRepository.save(project);
        
        // Add the current user as the owner of the project
        ProjectMember projectMember = ProjectMember.builder()
                .project(savedProject)
                .userId(currentUser.getId())
                .userName(currentUser.getName())
                .userEmail(currentUser.getEmail())
                .role(ProjectMember.ProjectRole.OWNER)
                .build();
        
        projectMemberRepository.save(projectMember);
        
        log.info("Project created successfully: {}", savedProject.getName());
        
        return mapToDto(savedProject);
    }
    
    /**
     * Update project
     */
    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Check if the user has permission to update the project
        if (!projectSecurity.isProjectManager(id)) {
            throw new AccessDeniedException("You don't have permission to update this project");
        }
        
        // Update project fields
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setStartDate(projectDto.getStartDate());
        project.setEndDate(projectDto.getEndDate());
        
        if (projectDto.getProgress() != null) {
            project.setProgress(projectDto.getProgress());
        }
        
        if (projectDto.getStatus() != null) {
            project.setStatus(projectDto.getStatus());
            
            // Update progress based on status if necessary
            if (projectDto.getStatus() == Project.ProjectStatus.COMPLETED && project.getProgress() < 100) {
                project.setProgress(100);
            } else if (projectDto.getStatus() == Project.ProjectStatus.IN_PROGRESS && project.getProgress() == 0) {
                project.setProgress(1); // Set to at least 1% if status is changed to in progress
            }
        }
        
        Project updatedProject = projectRepository.save(project);
        
        log.info("Project updated successfully: {}", updatedProject.getName());
        
        return mapToDto(updatedProject);
    }
    
    /**
     * Delete project
     */
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Check if the user has permission to delete the project
        if (!projectSecurity.isProjectOwner(id)) {
            throw new AccessDeniedException("You don't have permission to delete this project");
        }
        
        projectRepository.delete(project);
        log.info("Project deleted successfully: {}", project.getName());
    }
    
    /**
     * Update project progress
     */
    @Transactional
    public ProjectDto updateProjectProgress(Long id, Integer progress) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Check if the user has permission to update the project
        if (!projectSecurity.isProjectMember(id)) {
            throw new AccessDeniedException("You don't have permission to update this project");
        }
        
        // Validate progress
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        
        // Update progress
        project.setProgress(progress);
        
        // Update status based on progress if necessary
        if (progress == 100 && project.getStatus() != Project.ProjectStatus.COMPLETED) {
            project.setStatus(Project.ProjectStatus.COMPLETED);
        } else if (progress > 0 && progress < 100 && project.getStatus() == Project.ProjectStatus.NOT_STARTED) {
            project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        } else if (progress == 0 && project.getStatus() == Project.ProjectStatus.IN_PROGRESS) {
            project.setStatus(Project.ProjectStatus.NOT_STARTED);
        }
        
        Project updatedProject = projectRepository.save(project);
        
        log.info("Project progress updated successfully: {} - {}%", updatedProject.getName(), progress);
        
        return mapToDto(updatedProject);
    }
    
    /**
     * Get projects by user ID
     */
    public List<ProjectDto> getProjectsByUserId(Long userId) {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        // Users can only see their own projects unless they are admins
        if (!currentUser.getId().equals(userId) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You don't have permission to view other users' projects");
        }
        
        List<Project> projects = projectRepository.findByMemberId(userId);
        
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get projects by status
     */
    public List<ProjectDto> getProjectsByStatus(Project.ProjectStatus status) {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        List<Project> projects;
        
        if (currentUser.isAdmin()) {
            // Admin users can see all projects
            projects = projectRepository.findByStatus(status);
        } else {
            // Other users can only see projects they are members of
            projects = projectRepository.findByMemberId(currentUser.getId()).stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }
        
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming projects (due within specified days)
     */
    public List<ProjectDto> getUpcomingProjects(int days) {
        UserPrincipal currentUser = projectSecurity.getCurrentUser();
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        List<Project> projects;
        
        if (currentUser.isAdmin()) {
            // Admin users can see all projects
            projects = projectRepository.findByEndDateBetween(today, endDate);
        } else {
            // Other users can only see projects they are members of
            projects = projectRepository.findByMemberId(currentUser.getId()).stream()
                    .filter(p -> !p.getEndDate().isBefore(today) && !p.getEndDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }
        
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Project entity to ProjectDto
     */
    private ProjectDto mapToDto(Project project) {
        List<ProjectMemberDto> members = projectMemberRepository.findByProjectId(project.getId()).stream()
                .map(member -> ProjectMemberDto.builder()
                        .id(member.getId())
                        .projectId(project.getId())
                        .userId(member.getUserId())
                        .userName(member.getUserName())
                        .userEmail(member.getUserEmail())
                        .role(member.getRole())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
                
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .progress(project.getProgress())
                .status(project.getStatus())
                .tasksCount(project.getTasksCount())
                .createdBy(project.getCreatedBy())
                .createdByName(project.getCreatedByName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .members(members)
                .build();
    }
}