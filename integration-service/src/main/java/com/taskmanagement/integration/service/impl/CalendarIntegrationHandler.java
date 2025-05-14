package com.taskmanagement.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
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
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarIntegrationHandler implements IntegrationHandler {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String APPLICATION_NAME = "Task Management Calendar Integration";
    
    @Value("${integration.google.client-id}")
    private String clientId;
    
    @Value("${integration.google.client-secret}")
    private String clientSecret;
    
    private final TaskServiceClient taskServiceClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean connect(Integration integration, IntegrationConnectRequest request) {
        try {
            if (request.getAuthCode() == null || request.getRedirectUri() == null) {
                throw new IntegrationException("Auth code and redirect URI are required for Google Calendar integration");
            }
            
            // Build client secrets
            GoogleClientSecrets clientSecrets = buildClientSecrets();
            
            // Build authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES)
                    .build();
            
            // Exchange auth code for tokens
            TokenResponse tokenResponse = flow.newTokenRequest(request.getAuthCode())
                    .setRedirectUri(request.getRedirectUri())
                    .execute();
            
            // Store the tokens
            integration.setAccessToken(tokenResponse.getAccessToken());
            integration.setRefreshToken(tokenResponse.getRefreshToken());
            
            // Set token expiry
            if (tokenResponse.getExpiresInSeconds() != null) {
                LocalDateTime expiry = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());
                integration.setTokenExpiry(expiry);
            }
            
            // Store additional settings if provided
            if (request.getSettings() != null) {
                Map<String, Object> settings = new HashMap<>(request.getSettings());
                // Add default settings if needed
                if (!settings.containsKey("calendarId")) {
                    settings.put("calendarId", "primary");
                }
                integration.setSettings(objectMapper.writeValueAsString(settings));
            } else {
                // Set default settings
                Map<String, Object> settings = new HashMap<>();
                settings.put("calendarId", "primary");
                settings.put("syncEnabled", true);
                settings.put("syncFrequency", "daily");
                settings.put("createEvents", true);
                integration.setSettings(objectMapper.writeValueAsString(settings));
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error connecting to Google Calendar: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to connect to Google Calendar: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean disconnect(Integration integration) {
        // There's no specific API call to revoke access for Google Calendar
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
            // Get calendar settings
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = objectMapper.readValue(integration.getSettings(), Map.class);
            String calendarId = (String) settings.getOrDefault("calendarId", "primary");
            boolean createEvents = (boolean) settings.getOrDefault("createEvents", true);
            
            // Build Google Calendar service
            Calendar service = buildCalendarService(integration);
            
            // Get tasks from task service
            String authHeader = "Bearer " + integration.getAccessToken(); // Assuming the token is valid for both services
            List<TaskDto> tasks = taskServiceClient.getTasksForUser(authHeader, integration.getUserId());
            
            // Process each task
            for (TaskDto task : tasks) {
                itemsProcessed++;
                
                try {
                    // Check if we should create events for this task
                    if (createEvents && task.getDueDate() != null) {
                        // Check if event already exists by querying for events with summary = task title
                        String query = "summary=\"" + task.getTitle() + "\"";
                        Events events = service.events().list(calendarId)
                                .setQ(query)
                                .setTimeMin(toDateTime(LocalDateTime.now().minusDays(30)))
                                .setTimeMax(toDateTime(LocalDateTime.now().plusDays(60)))
                                .setMaxResults(10)
                                .execute();
                        
                        if (events.getItems() == null || events.getItems().isEmpty()) {
                            // Create new event
                            Event event = new Event()
                                    .setSummary(task.getTitle())
                                    .setDescription(task.getDescription() + "\n\nTask ID: " + task.getId());
                            
                            // Convert due date to DateTime
                            LocalDateTime dueDateTime = task.getDueDate().atTime(17, 0); // Default to 5PM
                            DateTime startDateTime = toDateTime(dueDateTime);
                            DateTime endDateTime = toDateTime(dueDateTime.plusHours(1));
                            
                            EventDateTime start = new EventDateTime().setDateTime(startDateTime);
                            EventDateTime end = new EventDateTime().setDateTime(endDateTime);
                            
                            event.setStart(start);
                            event.setEnd(end);
                            
                            // Insert event
                            service.events().insert(calendarId, event).execute();
                            successCount++;
                        } else {
                            // Event already exists, skip
                            successCount++;
                        }
                    } else {
                        // Skip tasks without due date or if createEvents is disabled
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
            log.error("Error syncing with Google Calendar: {}", e.getMessage(), e);
            throw new IntegrationException("Failed to sync with Google Calendar: " + e.getMessage(), e);
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
            // Build Google Calendar service
            Calendar service = buildCalendarService(integration);
            
            // Try to get calendars list as a simple validation
            service.calendarList().list().setMaxResults(1).execute();
            
            return true;
        } catch (Exception e) {
            log.error("Error validating Google Calendar credentials: {}", e.getMessage());
            return false;
        }
    }
    
    private GoogleClientSecrets buildClientSecrets() {
        // Build client secrets from environment variables
        String clientSecretsJson = "{\n" +
                "\"installed\": {\n" +
                "  \"client_id\": \"" + clientId + "\",\n" +
                "  \"client_secret\": \"" + clientSecret + "\"\n" +
                "}\n" +
                "}";
        
        try {
            return GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecretsJson));
        } catch (IOException e) {
            throw new IntegrationException("Failed to build Google client secrets", e);
        }
    }
    
    private Calendar buildCalendarService(Integration integration) throws GeneralSecurityException, IOException {
        // Build HTTP transport
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // Use the GoogleClientSecrets and flow to create credentials
        GoogleClientSecrets clientSecrets = buildClientSecrets();
        
        // Create a simple credential with the token
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES).build();
                
        // Create a credential directly
        GoogleClientSecrets.Details details = clientSecrets.getInstalled();
        
        // Create a token response manually
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(integration.getAccessToken());
        tokenResponse.setRefreshToken(integration.getRefreshToken());
        
        // Set expiration if available
        if (integration.getTokenExpiry() != null) {
            long expiresInSeconds = (integration.getTokenExpiry().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() 
                - System.currentTimeMillis()) / 1000;
            tokenResponse.setExpiresInSeconds(expiresInSeconds);
        }
        
        // Create credential from the token response
        Credential credential = flow.createAndStoreCredential(tokenResponse, "user");
        
        // Build calendar service
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    private DateTime toDateTime(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return new DateTime(instant.toEpochMilli());
    }
}