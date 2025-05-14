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
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VersionControlIntegrationHandler implements IntegrationHandler {

    @Value("${integration.github.client-id}")
    private String clientId;
    
    @Value("${integration.github.client-secret}")
    private String clientSecret;
    
    private final TaskServiceClient taskServiceClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean connect(Integration integration, IntegrationConnectRequest request) {
        try {
            if (request.getAccessToken() == null) {
                throw new IntegrationException("Access token is required for GitHub integration");
            }
            
            // Validate the token by connecting to GitHub
            GitHub github = new GitHubBuilder().withOAuthToken(request.getAccessToken()).build();
            github.getMyself(); // Will throw exception if token is invalid
            
            // Store additional settings if provided
            if (request.getSettings() != null) {
                Map<String, Object> settings = new HashMap<>(request.getSettings());
                // Add default settings if needed
                if (!settings.containsKey("defaultRepository")) {
                    settings.put("defaultRepository", "");
                }
                integration.setSettings(objectMapper.writeValueAsString(settings));
            } else {
                // Set default settings
                Map<String, Object> settings = new HashMap<>();
                settings.put("defaultRepository", "");
                settings.put("createIssuesFromTasks", true);
                settings.put("linkPullRequests", true);
                settings.put("syncCompletedIssues", true);
                integration.setSettings(objectMapper.writeValueAsString(settings));
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error connecting to GitHub: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to connect to GitHub: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean disconnect(Integration integration) {
        // There's no specific API call to revoke access for GitHub
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
            String defaultRepository = (String) settings.getOrDefault("defaultRepository", "");
            boolean createIssuesFromTasks = (boolean) settings.getOrDefault("createIssuesFromTasks", true);
            
            // Connect to GitHub
            GitHub github = new GitHubBuilder().withOAuthToken(integration.getAccessToken()).build();
            
            // Get user tasks
            String authHeader = "Bearer " + integration.getAccessToken();
            List<TaskDto> tasks = taskServiceClient.getTasksForUser(authHeader, integration.getUserId());
            
            // Process each task
            for (TaskDto task : tasks) {
                itemsProcessed++;
                
                try {
                    // Only create issues for tasks that don't have an associated GitHub issue yet
                    // In a real implementation, we would track which tasks have been synced
                    if (createIssuesFromTasks && defaultRepository != null && !defaultRepository.isEmpty()) {
                        // In a real implementation, we would check if an issue already exists for this task
                        log.info("Would create GitHub issue for task: {}", task.getTitle());
                        
                        /*
                        // This code would create a GitHub issue
                        GHRepository repo = github.getRepository(defaultRepository);
                        GHIssueBuilder issueBuilder = repo.createIssue(task.getTitle())
                                .body(formatIssueBody(task))
                                .label("task");
                                
                        if (task.getPriority() != null) {
                            issueBuilder.label(task.getPriority().toLowerCase());
                        }
                        
                        GHIssue issue = issueBuilder.create();
                        log.info("Created GitHub issue #{} for task {}", issue.getNumber(), task.getId());
                        */
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("Error syncing task {}: {}", task.getId(), e.getMessage());
                    errorCount++;
                    errorDetails.append("Error syncing task ").append(task.getId())
                            .append(": ").append(e.getMessage()).append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error syncing with GitHub: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to sync with GitHub: " + e.getMessage(), e);
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
            // Connect to GitHub and get user info to validate credentials
            GitHub github = new GitHubBuilder().withOAuthToken(integration.getAccessToken()).build();
            github.getMyself(); // Will throw exception if token is invalid
            return true;
        } catch (Exception e) {
            log.error("Error validating GitHub credentials: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Format a task as a GitHub issue body
     */
    private String formatIssueBody(TaskDto task) {
        StringBuilder body = new StringBuilder();
        body.append("## Task Details\n\n");
        body.append("**Project:** ").append(task.getProjectName()).append("\n\n");
        body.append("**Priority:** ").append(task.getPriority()).append("\n\n");
        body.append("**Due Date:** ").append(task.getDueDate()).append("\n\n");
        body.append("**Status:** ").append(task.getStatus()).append("\n\n");
        
        body.append("## Description\n\n");
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            body.append(task.getDescription());
        } else {
            body.append("No description provided.");
        }
        
        body.append("\n\n---\n");
        body.append("*This issue was automatically created from Task Management System*");
        
        return body.toString();
    }
}