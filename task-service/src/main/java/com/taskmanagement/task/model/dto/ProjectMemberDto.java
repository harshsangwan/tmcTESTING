package com.taskmanagement.task.model.dto;

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
    private Long userId;
    private String userName;
    private String userEmail;
    private String role;
    private LocalDateTime joinedAt;
}