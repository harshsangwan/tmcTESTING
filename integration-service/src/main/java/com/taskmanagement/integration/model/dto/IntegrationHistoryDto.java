package com.taskmanagement.integration.model.dto;

import com.taskmanagement.integration.model.entity.IntegrationHistory.ActionType;
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
public class IntegrationHistoryDto {
    
    private Long id;
    private Long integrationId;
    private String integrationName;
    private ActionType actionType;
    private String actionDetails;
    private Integer itemsProcessed;
    private Integer successCount;
    private Integer errorCount;
    private Map<String, Object> errorDetails;
    private Long durationMs;
    private LocalDateTime createdAt;
}