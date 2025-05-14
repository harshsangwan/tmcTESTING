package com.taskmanagement.integration.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.integration.model.entity.Integration.IntegrationType;
import com.taskmanagement.integration.model.entity.Integration.IntegrationStatus;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrationDto {
    
    private Long id;
    private String name;
    private String description;
    private IntegrationType type;
    private IntegrationStatus status;
    private String connectionUrl;
    private LocalDateTime lastSyncDate;
    private Map<String, Object> settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Fields that will not be included in responses
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String apiKey;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accessToken;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> credentials;
}