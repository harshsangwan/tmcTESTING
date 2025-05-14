package com.taskmanagement.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.integration.exception.IntegrationException;
import com.taskmanagement.integration.exception.ResourceNotFoundException;
import com.taskmanagement.integration.model.dto.IntegrationConnectRequest;
import com.taskmanagement.integration.model.dto.IntegrationDto;
import com.taskmanagement.integration.model.entity.Integration;
import com.taskmanagement.integration.model.entity.Integration.IntegrationStatus;
import com.taskmanagement.integration.model.entity.Integration.IntegrationType;
import com.taskmanagement.integration.model.entity.IntegrationHistory;
import com.taskmanagement.integration.model.entity.IntegrationHistory.ActionType;
import com.taskmanagement.integration.repository.IntegrationHistoryRepository;
import com.taskmanagement.integration.repository.IntegrationRepository;
import com.taskmanagement.integration.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationHistoryRepository integrationHistoryRepository;
    private final IntegrationFactoryService integrationFactoryService;
    private final ObjectMapper objectMapper;

    /**
     * Get all integrations for the current user
     */
    public List<IntegrationDto> getUserIntegrations(UserPrincipal currentUser) {
        List<Integration> integrations = integrationRepository.findByUserId(currentUser.getId());
        return integrations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get integration by ID
     */
    public IntegrationDto getIntegrationById(Long id, UserPrincipal currentUser) {
        Integration integration = integrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + id));
        
        // Verify the integration belongs to the current user
        if (!integration.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new IntegrationException("You don't have permission to access this integration");
        }
        
        return mapToDto(integration);
    }
    
    /**
     * Connect to an integration
     */
    @Transactional
    public IntegrationDto connectIntegration(IntegrationConnectRequest request, UserPrincipal currentUser) {
        long startTime = System.currentTimeMillis();
        
        Integration integration = integrationRepository.findById(request.getIntegrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + request.getIntegrationId()));
        
        // Verify the integration belongs to the current user
        if (!integration.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new IntegrationException("You don't have permission to access this integration");
        }
        
        try {
            // Use the integration factory to get the appropriate handler
            IntegrationHandler handler = integrationFactoryService.getHandler(integration.getType());
            
            // Connect to the integration
            handler.connect(integration, request);
            
            // Update integration status
            integration.setStatus(IntegrationStatus.CONNECTED);
            integration.setLastSyncDate(LocalDateTime.now());
            
            // Store credentials securely
            Map<String, Object> credentials = new HashMap<>();
            if (request.getApiKey() != null) {
                integration.setApiKey(request.getApiKey());
            }
            if (request.getAccessToken() != null) {
                integration.setAccessToken(request.getAccessToken());
            }
            if (request.getRefreshToken() != null) {
                integration.setRefreshToken(request.getRefreshToken());
            }
            if (request.getCredentials() != null) {
                credentials.putAll(request.getCredentials());
                integration.setCredentials(objectMapper.writeValueAsString(credentials));
            }
            
            // Store settings
            if (request.getSettings() != null) {
                integration.setSettings(objectMapper.writeValueAsString(request.getSettings()));
            }
            
            Integration savedIntegration = integrationRepository.save(integration);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            createHistoryEntry(
                    savedIntegration,
                    ActionType.CONNECTED,
                    "Successfully connected to " + integration.getName(),
                    1,
                    1,
                    0,
                    null,
                    duration,
                    currentUser.getId()
            );
            
            log.info("Integration connected successfully: {}", savedIntegration.getName());
            
            return mapToDto(savedIntegration);
        } catch (Exception e) {
            log.error("Error connecting to integration: {}", e.getMessage(), e);
            
            // Update integration status to error
            integration.setStatus(IntegrationStatus.ERROR);
            Integration savedIntegration = integrationRepository.save(integration);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            createHistoryEntry(
                    savedIntegration,
                    ActionType.ERROR_OCCURRED,
                    "Error connecting to " + integration.getName(),
                    0,
                    0,
                    1,
                    e.getMessage(),
                    duration,
                    currentUser.getId()
            );
            
            throw new IntegrationException("Failed to connect to integration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Disconnect from an integration
     */
    @Transactional
    public IntegrationDto disconnectIntegration(Long id, UserPrincipal currentUser) {
        long startTime = System.currentTimeMillis();
        
        Integration integration = integrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + id));
        
        // Verify the integration belongs to the current user
        if (!integration.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new IntegrationException("You don't have permission to access this integration");
        }
        
        try {
            // Use the integration factory to get the appropriate handler
            IntegrationHandler handler = integrationFactoryService.getHandler(integration.getType());
            
            // Disconnect from the integration
            handler.disconnect(integration);
            
            // Update integration status
            integration.setStatus(IntegrationStatus.DISCONNECTED);
            integration.setAccessToken(null);
            integration.setRefreshToken(null);
            integration.setTokenExpiry(null);
            
            Integration savedIntegration = integrationRepository.save(integration);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            createHistoryEntry(
                    savedIntegration,
                    ActionType.DISCONNECTED,
                    "Successfully disconnected from " + integration.getName(),
                    1,
                    1,
                    0,
                    null,
                    duration,
                    currentUser.getId()
            );
            
            log.info("Integration disconnected successfully: {}", savedIntegration.getName());
            
            return mapToDto(savedIntegration);
        } catch (Exception e) {
            log.error("Error disconnecting from integration: {}", e.getMessage(), e);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            createHistoryEntry(
                    integration,
                    ActionType.ERROR_OCCURRED,
                    "Error disconnecting from " + integration.getName(),
                    0,
                    0,
                    1,
                    e.getMessage(),
                    duration,
                    currentUser.getId()
            );
            
            throw new IntegrationException("Failed to disconnect from integration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sync data with an integration
     */
    @Transactional
    public IntegrationDto syncIntegration(Long id, UserPrincipal currentUser) {
        long startTime = System.currentTimeMillis();
        
        Integration integration = integrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + id));
        
        // Verify the integration belongs to the current user
        if (!integration.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new IntegrationException("You don't have permission to access this integration");
        }
        
        // Check if integration is connected
        if (integration.getStatus() != IntegrationStatus.CONNECTED) {
            throw new IntegrationException("Integration is not connected");
        }
        
        try {
            // Use the integration factory to get the appropriate handler
            IntegrationHandler handler = integrationFactoryService.getHandler(integration.getType());
            
            // Sync data with the integration
            Map<String, Object> syncResult = handler.sync(integration);
            
            // Update integration
            integration.setLastSyncDate(LocalDateTime.now());
            
            Integration savedIntegration = integrationRepository.save(integration);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            
            // Extract sync statistics
            int itemsProcessed = (Integer) syncResult.getOrDefault("itemsProcessed", 0);
            int successCount = (Integer) syncResult.getOrDefault("successCount", 0);
            int errorCount = (Integer) syncResult.getOrDefault("errorCount", 0);
            String errorDetails = (String) syncResult.getOrDefault("errorDetails", null);
            
            createHistoryEntry(
                    savedIntegration,
                    ActionType.SYNCED,
                    "Synchronized data with " + integration.getName(),
                    itemsProcessed,
                    successCount,
                    errorCount,
                    errorDetails,
                    duration,
                    currentUser.getId()
            );
            
            log.info("Integration synced successfully: {}", savedIntegration.getName());
            
            return mapToDto(savedIntegration);
        } catch (Exception e) {
            log.error("Error syncing integration: {}", e.getMessage(), e);
            
            // Create integration history entry
            long duration = System.currentTimeMillis() - startTime;
            createHistoryEntry(
                    integration,
                    ActionType.ERROR_OCCURRED,
                    "Error syncing with " + integration.getName(),
                    0,
                    0,
                    1,
                    e.getMessage(),
                    duration,
                    currentUser.getId()
            );
            
            throw new IntegrationException("Failed to sync integration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get integration history
     */
    public List<IntegrationHistory> getIntegrationHistory(Long integrationId, UserPrincipal currentUser) {
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration not found with id: " + integrationId));
        
        // Verify the integration belongs to the current user
        if (!integration.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new IntegrationException("You don't have permission to access this integration");
        }
        
        return integrationHistoryRepository.findByIntegrationIdOrderByCreatedAtDesc(integrationId);
    }
    
    /**
     * Create a new integration history entry
     */
    private void createHistoryEntry(Integration integration, ActionType actionType, String actionDetails,
                                   Integer itemsProcessed, Integer successCount, Integer errorCount,
                                   String errorDetails, Long durationMs, Long userId) {
        IntegrationHistory history = IntegrationHistory.builder()
                .integration(integration)
                .actionType(actionType)
                .actionDetails(actionDetails)
                .itemsProcessed(itemsProcessed)
                .successCount(successCount)
                .errorCount(errorCount)
                .errorDetails(errorDetails)
                .durationMs(durationMs)
                .userId(userId)
                .build();
        
        integrationHistoryRepository.save(history);
    }
    
    /**
     * Map Integration entity to IntegrationDto
     */
    private IntegrationDto mapToDto(Integration integration) {
        IntegrationDto dto = IntegrationDto.builder()
                .id(integration.getId())
                .name(integration.getName())
                .description(integration.getDescription())
                .type(integration.getType())
                .status(integration.getStatus())
                .connectionUrl(integration.getConnectionUrl())
                .lastSyncDate(integration.getLastSyncDate())
                .createdAt(integration.getCreatedAt())
                .updatedAt(integration.getUpdatedAt())
                .build();
        
        // Map settings if present
        if (integration.getSettings() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = objectMapper.readValue(integration.getSettings(), Map.class);
                dto.setSettings(settings);
            } catch (JsonProcessingException e) {
                log.error("Error parsing integration settings: {}", e.getMessage());
            }
        }
        
        return dto;
    }
}