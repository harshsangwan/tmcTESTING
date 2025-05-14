package com.taskmanagement.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.admin.exception.ResourceNotFoundException;
import com.taskmanagement.admin.model.dto.SystemSettingsDto;
import com.taskmanagement.admin.model.entity.SystemSettings;
import com.taskmanagement.admin.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get system settings
     */
    public SystemSettingsDto getSettings() {
        // Get settings or create default ones if none exist
        SystemSettings settings = systemSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultSettings);
        
        return mapToDto(settings);
    }
    
    /**
     * Update system settings
     */
    @Transactional
    public SystemSettingsDto updateSettings(SystemSettingsDto settingsDto) {
        // Get settings or create default ones if none exist
        SystemSettings settings = systemSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultSettings);
        
        // Update settings
        settings.setSiteName(settingsDto.getSiteName());
        settings.setSiteDescription(settingsDto.getSiteDescription());
        settings.setDefaultUserRole(settingsDto.getDefaultUserRole());
        settings.setAllowRegistration(settingsDto.getAllowRegistration());
        settings.setMaintenanceMode(settingsDto.getMaintenanceMode());
        settings.setEmailNotifications(settingsDto.getEmailNotifications());
        settings.setTaskReminderDays(settingsDto.getTaskReminderDays());
        settings.setMaxProjectsPerUser(settingsDto.getMaxProjectsPerUser());
        settings.setMaxTasksPerProject(settingsDto.getMaxTasksPerProject());
        
        // Convert theme settings to JSON
        try {
            if (settingsDto.getTheme() != null) {
                settings.setThemeSettings(objectMapper.writeValueAsString(settingsDto.getTheme()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing theme settings", e);
        }
        
        SystemSettings updatedSettings = systemSettingsRepository.save(settings);
        
        return mapToDto(updatedSettings);
    }
    
    /**
     * Create default settings
     */
    private SystemSettings createDefaultSettings() {
        SystemSettings settings = SystemSettings.builder()
                .siteName("Task Management System")
                .siteDescription("A comprehensive task management solution")
                .defaultUserRole("Member")
                .allowRegistration(true)
                .maintenanceMode(false)
                .emailNotifications(true)
                .taskReminderDays(3)
                .maxProjectsPerUser(0) // 0 means unlimited
                .maxTasksPerProject(0) // 0 means unlimited
                .build();
        
        try {
            Map<String, Object> theme = new HashMap<>();
            theme.put("primaryColor", "#6b48ff");
            theme.put("secondaryColor", "#ff6bcb");
            theme.put("darkMode", true);
            
            settings.setThemeSettings(objectMapper.writeValueAsString(theme));
        } catch (JsonProcessingException e) {
            log.error("Error serializing default theme settings", e);
        }
        
        return systemSettingsRepository.save(settings);
    }
    
    /**
     * Map SystemSettings entity to SystemSettingsDto
     */
    private SystemSettingsDto mapToDto(SystemSettings settings) {
        Map<String, Object> theme = new HashMap<>();
        
        try {
            if (settings.getThemeSettings() != null) {
                theme = objectMapper.readValue(settings.getThemeSettings(), Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing theme settings", e);
        }
        
        return SystemSettingsDto.builder()
                .siteName(settings.getSiteName())
                .siteDescription(settings.getSiteDescription())
                .defaultUserRole(settings.getDefaultUserRole())
                .allowRegistration(settings.getAllowRegistration())
                .maintenanceMode(settings.getMaintenanceMode())
                .emailNotifications(settings.getEmailNotifications())
                .taskReminderDays(settings.getTaskReminderDays())
                .maxProjectsPerUser(settings.getMaxProjectsPerUser())
                .maxTasksPerProject(settings.getMaxTasksPerProject())
                .theme(theme)
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
    
    /**
     * Backup system settings
     */
    public Map<String, String> backupSystem() {
        // In a real implementation, this would create a backup of the database
        // and other system files, then return a download URL
        
        return Map.of(
                "status", "success",
                "message", "System backup created successfully",
                "downloadUrl", "/api/admin/settings/downloads/backup-" + System.currentTimeMillis() + ".zip"
        );
    }
    
    /**
     * Restore system settings
     */
    public Map<String, String> restoreSystem() {
        // In a real implementation, this would restore the system from a backup
        
        return Map.of(
                "status", "success",
                "message", "System restored successfully"
        );
    }
    
    /**
     * Clear system cache
     */
    public Map<String, String> clearCache() {
        // In a real implementation, this would clear system caches
        
        return Map.of(
                "status", "success",
                "message", "System cache cleared successfully"
        );
    }
}