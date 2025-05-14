package com.taskmanagement.project.controller;

import com.taskmanagement.project.model.dto.ProjectDto;
import com.taskmanagement.project.model.entity.Project.ProjectStatus;
import com.taskmanagement.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        log.info("Request to get all projects");
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        log.info("Request to get project by id: {}", id);
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        log.info("Request to create new project: {}", projectDto.getName());
        return ResponseEntity.ok(projectService.createProject(projectDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDto projectDto) {
        log.info("Request to update project with id: {}", id);
        return ResponseEntity.ok(projectService.updateProject(id, projectDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Request to delete project with id: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ProjectDto> updateProjectProgress(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody) {
        Integer progress = requestBody.get("progress");
        if (progress == null) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Request to update progress of project with id: {} to {}%", id, progress);
        return ResponseEntity.ok(projectService.updateProjectProgress(id, progress));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectDto>> getProjectsByUserId(@PathVariable Long userId) {
        log.info("Request to get projects for user: {}", userId);
        return ResponseEntity.ok(projectService.getProjectsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectDto>> getProjectsByStatus(@PathVariable String status) {
        try {
            ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
            log.info("Request to get projects with status: {}", projectStatus);
            return ResponseEntity.ok(projectService.getProjectsByStatus(projectStatus));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ProjectDto>> getUpcomingProjects(@RequestParam(defaultValue = "7") int days) {
        log.info("Request to get projects due in the next {} days", days);
        return ResponseEntity.ok(projectService.getUpcomingProjects(days));
    }
}