package com.taskmanagement.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.integration.client.TaskServiceClient;
import com.taskmanagement.integration.exception.IntegrationException;
import com.taskmanagement.integration.model.dto.IntegrationConnectRequest;
import com.taskmanagement.integration.model.dto.TaskDto;
import com.taskmanagement.integration.model.entity.Integration;
import com.taskmanagement.integration.service.IntegrationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageIntegrationHandler implements IntegrationHandler {

    @Value("${integration.dropbox.client-id:dropbox-client-id}")
    private String clientId;
    
    @Value("${integration.dropbox.client-secret:dropbox-client-secret}")
    private String clientSecret;
    
    private final TaskServiceClient taskServiceClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean connect(Integration integration, IntegrationConnectRequest request) {
        try {
            if (request.getAccessToken() == null) {
                throw new IntegrationException("Access token is required for storage integration");
            }
            
            // Validate the token by making a simple API call
            if (!validateCredentials(integration)) {
                throw new IntegrationException("Invalid storage access token");
            }
            
            // Store additional settings if provided
            if (request.getSettings() != null) {
                Map<String, Object> settings = new HashMap<>(request.getSettings());
                // Add default settings if needed
                if (!settings.containsKey("defaultFolder")) {
                    settings.put("defaultFolder", "/Task Management");
                }
                integration.setSettings(objectMapper.writeValueAsString(settings));
            } else {
                // Set default settings
                Map<String, Object> settings = new HashMap<>();
                settings.put("defaultFolder", "/Task Management");
                settings.put("autoCreateFolders", true);
                settings.put("folderNamingPattern", "PROJECT_NAME/TASK_NAME");
                integration.setSettings(objectMapper.writeValueAsString(settings));
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error connecting to storage service: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to connect to storage service: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean disconnect(Integration integration) {
        // Simply return true since there's usually no revoke endpoint for storage services
        return true;
    }
    
    @Override
    public Map<String, Object> sync(Integration integration) {
        Map<String, Object> result = new HashMap<>();
        int itemsProcessed = 0;
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errorDetails = new StringBuilder();
        
        try {
            // Get settings
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = objectMapper.readValue(integration.getSettings(), Map.class);
            String defaultFolder = (String) settings.getOrDefault("defaultFolder", "/Task Management");
            boolean autoCreateFolders = (boolean) settings.getOrDefault("autoCreateFolders", true);
            
            // Get user tasks
            String authHeader = "Bearer " + integration.getAccessToken();
            List<TaskDto> tasks = taskServiceClient.getTasksForUser(authHeader, integration.getUserId());
            
            // Process each task
            for (TaskDto task : tasks) {
                itemsProcessed++;
                
                try {
                    // In a real implementation, this would create folders for each task if needed
                    // and possibly sync any task attachments
                    // For this example, we'll just simulate success
                    if (autoCreateFolders) {
                        String folderPath = defaultFolder + "/" + task.getProjectName() + "/" + task.getTitle();
                        // Here would be code to create the folder in Dropbox/Google Drive/etc.
                        log.info("Would create folder: {}", folderPath);
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("Error processing task {}: {}", task.getId(), e.getMessage());
                    errorCount++;
                    errorDetails.append("Error processing task ").append(task.getId())
                            .append(": ").append(e.getMessage()).append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error syncing with storage service: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to sync with storage service: " + e.getMessage(), e);
        }
        
        // Set result
        result.put("itemsProcessed", itemsProcessed);
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        if (errorCount > 0) {
            result.put("errorDetails", errorDetails.toString());
        }
        
        return result;
    }
    
    @Override
    public boolean validateCredentials(Integration integration) {
        try {
            // For Dropbox, we could make a simple API call like getting account info
            // This is a simplified example
            String accessToken = integration.getAccessToken();
            
            URL url = new URL("https://api.dropboxapi.com/2/users/get_current_account");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            log.error("Error validating storage credentials: {}", e.getMessage());
            return false;
        }
    }
}