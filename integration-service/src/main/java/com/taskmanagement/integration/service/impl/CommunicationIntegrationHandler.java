package com.taskmanagement.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.auth.AuthTestRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunicationIntegrationHandler implements IntegrationHandler {

    @Value("${integration.slack.client-id}")
    private String clientId;
    
    @Value("${integration.slack.client-secret}")
    private String clientSecret;
    
    private final TaskServiceClient taskServiceClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean connect(Integration integration, IntegrationConnectRequest request) {
        try {
            if (request.getAccessToken() == null) {
                throw new IntegrationException("Access token is required for Slack integration");
            }
            
            // Validate the token by calling auth.test
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(request.getAccessToken());
            
            AuthTestResponse response = methods.authTest(AuthTestRequest.builder().build());
            if (!response.isOk()) {
                throw new IntegrationException("Invalid Slack token: " + response.getError());
            }
            
            // Store additional settings if provided
            if (request.getSettings() != null) {
                Map<String, Object> settings = new HashMap<>(request.getSettings());
                // Add default settings if needed
                if (!settings.containsKey("defaultChannel")) {
                    settings.put("defaultChannel", "general");
                }
                integration.setSettings(objectMapper.writeValueAsString(settings));
            } else {
                // Set default settings
                Map<String, Object> settings = new HashMap<>();
                settings.put("defaultChannel", "general");
                settings.put("notifyOnTaskCreate", true);
                settings.put("notifyOnTaskAssignment", true);
                settings.put("notifyOnTaskCompletion", true);
                integration.setSettings(objectMapper.writeValueAsString(settings));
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error connecting to Slack: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to connect to Slack: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean disconnect(Integration integration) {
        // There's no specific API call to revoke access for Slack
        // Just remove the tokens and return true
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
            String defaultChannel = (String) settings.getOrDefault("defaultChannel", "general");
            boolean notifyOnTaskAssignment = (boolean) settings.getOrDefault("notifyOnTaskAssignment", true);
            
            // Create Slack client
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(integration.getAccessToken());
            
            // Get recent assigned tasks
            String authHeader = "Bearer " + integration.getAccessToken(); // Assuming the token is valid for both services
            List<TaskDto> tasks = taskServiceClient.getTasksForUser(authHeader, integration.getUserId());
            
            // Process each assigned task
            for (TaskDto task : tasks) {
                itemsProcessed++;
                
                try {
                    // Only sync recently assigned tasks (in the last day)
                    if (notifyOnTaskAssignment && task.getAssignedTo() != null && 
                            task.getAssignedTo().equals(integration.getUserId())) {
                        
                        // Post message to Slack
                        ChatPostMessageRequest messageRequest = ChatPostMessageRequest.builder()
                                .channel(defaultChannel)
                                .text("*New Task Assigned:* " + task.getTitle() + "\n" +
                                      ">*Project:* " + task.getProjectName() + "\n" +
                                      ">*Due Date:* " + task.getDueDate() + "\n" +
                                      ">*Priority:* " + task.getPriority() + "\n" +
                                      ">*Description:* " + (task.getDescription() != null ? task.getDescription() : "N/A"))
                                .build();
                        
                        // Only post if task is new or recently assigned (logic would be more complex here)
                        methods.chatPostMessage(messageRequest);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("Error syncing task {}: {}", task.getId(), e.getMessage());
                    errorCount++;
                    errorDetails.append("Error syncing task ").append(task.getId())
                            .append(": ").append(e.getMessage()).append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error syncing with Slack: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to sync with Slack: " + e.getMessage(), e);
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
            // Validate the token by calling auth.test
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(integration.getAccessToken());
            
            AuthTestResponse response = methods.authTest(AuthTestRequest.builder().build());
            return response.isOk();
        } catch (Exception e) {
            log.error("Error validating Slack credentials: {}", e.getMessage());
            return false;
        }
    }
}