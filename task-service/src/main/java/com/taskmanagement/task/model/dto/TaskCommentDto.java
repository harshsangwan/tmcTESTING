package com.taskmanagement.task.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentDto {
    
    private Long id;
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String content;
    
    private Long userId;
    
    private String userName;
    
    private String userEmail;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}