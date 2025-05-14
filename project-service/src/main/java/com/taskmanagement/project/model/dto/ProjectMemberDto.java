package com.taskmanagement.project.model.dto;

import com.taskmanagement.project.model.entity.ProjectMember.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDto {
    
    private Long id;
    
    private Long projectId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String userName;
    
    private String userEmail;
    
    @NotNull(message = "Role is required")
    private ProjectRole role;
    
    private LocalDateTime joinedAt;
}