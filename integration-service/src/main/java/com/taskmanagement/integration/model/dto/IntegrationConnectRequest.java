package com.taskmanagement.integration.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConnectRequest {
    
    @NotNull(message = "Integration ID is required")
    private Long integrationId;
    
    private String apiKey;
    
    private String accessToken;
    
    private String refreshToken;
    
    private String authCode;
    
    private String redirectUri;
    
    private Map<String, Object> credentials;
    
    private Map<String, Object> settings;
}