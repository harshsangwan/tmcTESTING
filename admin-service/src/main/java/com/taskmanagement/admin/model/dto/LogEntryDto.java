package com.taskmanagement.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDto {
    
    private Long id;
    private LocalDateTime timestamp;
    private String level;
    private String source;
    private String message;
    private Long userId;
    private String userName;
    private String resourceType;
    private String resourceId;
    private Map<String, Object> metadata;
}