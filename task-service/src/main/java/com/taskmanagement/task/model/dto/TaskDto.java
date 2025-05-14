package com.taskmanagement.task.model.dto;

import com.taskmanagement.task.model.entity.Task.TaskStatus;
import com.taskmanagement.task.model.entity.Task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    
    private Long id;
    
    @NotBlank(message = "Task title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private String projectName;
    
    @NotNull(message = "Task status is required")
    private TaskStatus status;
    
    @NotNull(message = "Task priority is required")
    private TaskPriority priority;
    
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    private Long assignedTo;
    
    private String assigneeName;
    
    private Long createdBy;
    
    private String createdByName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    private List<TaskCommentDto> comments;
}