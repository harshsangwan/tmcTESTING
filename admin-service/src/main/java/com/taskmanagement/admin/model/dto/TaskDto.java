package com.taskmanagement.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectName;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Long assignedTo;
    private String assigneeName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}