package com.taskmanagement.project.controller;

import com.taskmanagement.project.model.dto.ProjectMemberDto;
import com.taskmanagement.project.model.entity.ProjectMember.ProjectRole;
import com.taskmanagement.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDto>> getProjectMembers(@PathVariable Long projectId) {
        log.info("Request to get members of project with id: {}", projectId);
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberDto> addProjectMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberDto memberDto) {
        log.info("Request to add member with userId: {} to project with id: {}", memberDto.getUserId(), projectId);
        return ResponseEntity.ok(projectMemberService.addProjectMember(projectId, memberDto));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ProjectMemberDto> updateProjectMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestBody) {
        
        String role = requestBody.get("role");
        if (role == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            ProjectRole projectRole = ProjectRole.valueOf(role.toUpperCase());
            log.info("Request to update role of user: {} in project: {} to {}", userId, projectId, projectRole);
            return ResponseEntity.ok(projectMemberService.updateProjectMemberRole(projectId, userId, projectRole));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", role);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        log.info("Request to remove user: {} from project: {}", userId, projectId);
        projectMemberService.removeProjectMember(projectId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveProject(@PathVariable Long projectId) {
        log.info("Request to leave project with id: {}", projectId);
        projectMemberService.leaveProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer-ownership")
    public ResponseEntity<Void> transferProjectOwnership(
            @PathVariable Long projectId,
            @RequestBody Map<String, Long> requestBody) {
        
        Long newOwnerId = requestBody.get("newOwnerId");
        if (newOwnerId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Request to transfer ownership of project: {} to user: {}", projectId, newOwnerId);
        projectMemberService.transferProjectOwnership(projectId, newOwnerId);
        return ResponseEntity.ok().build();
    }
}