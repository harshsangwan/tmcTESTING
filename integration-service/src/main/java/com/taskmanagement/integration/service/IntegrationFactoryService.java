package com.taskmanagement.integration.service;

import com.taskmanagement.integration.exception.IntegrationException;
import com.taskmanagement.integration.model.entity.Integration.IntegrationType;
import com.taskmanagement.integration.service.impl.CalendarIntegrationHandler;
import com.taskmanagement.integration.service.impl.CommunicationIntegrationHandler;
import com.taskmanagement.integration.service.impl.StorageIntegrationHandler;
import com.taskmanagement.integration.service.impl.VersionControlIntegrationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Factory service for getting the appropriate integration handler
 */
@Service
@RequiredArgsConstructor
public class IntegrationFactoryService {

    private final CalendarIntegrationHandler calendarIntegrationHandler;
    private final CommunicationIntegrationHandler communicationIntegrationHandler;
    private final VersionControlIntegrationHandler versionControlIntegrationHandler;
    private final StorageIntegrationHandler storageIntegrationHandler;
    
    /**
     * Get the appropriate integration handler for the given integration type
     * 
     * @param type The integration type
     * @return The integration handler
     */
    public IntegrationHandler getHandler(IntegrationType type) {
        switch (type) {
            case CALENDAR:
                return calendarIntegrationHandler;
            case COMMUNICATION:
                return communicationIntegrationHandler;
            case VERSION_CONTROL:
                return versionControlIntegrationHandler;
            case STORAGE:
                return storageIntegrationHandler;
            default:
                throw new IntegrationException("Unsupported integration type: " + type);
        }
    }
}