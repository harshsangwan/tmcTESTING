package com.taskmanagement.integration.service;

import com.taskmanagement.integration.model.dto.IntegrationConnectRequest;
import com.taskmanagement.integration.model.entity.Integration;

import java.util.Map;

/**
 * Interface for all integration handlers
 */
public interface IntegrationHandler {
    
    /**
     * Connect to the integration service
     * 
     * @param integration The integration to connect to
     * @param request The connection request with credentials
     * @return True if connection was successful
     */
    boolean connect(Integration integration, IntegrationConnectRequest request);
    
    /**
     * Disconnect from the integration service
     * 
     * @param integration The integration to disconnect from
     * @return True if disconnection was successful
     */
    boolean disconnect(Integration integration);
    
    /**
     * Sync data with the integration service
     * 
     * @param integration The integration to sync with
     * @return Map with sync results (itemsProcessed, successCount, errorCount, etc.)
     */
    Map<String, Object> sync(Integration integration);
    
    /**
     * Check if credentials are valid
     * 
     * @param integration The integration to validate credentials for
     * @return True if credentials are valid
     */
    boolean validateCredentials(Integration integration);
}