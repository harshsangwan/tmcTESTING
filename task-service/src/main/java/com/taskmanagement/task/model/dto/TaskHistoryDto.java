package com.taskmanagement.task.model.dto;

import com.taskmanagement.task.model.entity.TaskHistory.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDto {
    
    private Long id;
    
    private Long taskId;
    
    private ActionType actionType;
    
    private String oldValue;
    
    private String newValue;
    
    private String fieldName;
    
    private Long userId;
    
    private String userName;
    
    private LocalDateTime createdAt;
}