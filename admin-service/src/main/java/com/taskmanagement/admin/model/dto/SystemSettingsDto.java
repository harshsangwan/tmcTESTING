package com.taskmanagement.admin.model.dto;

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
public class SystemSettingsDto {
    
    private String siteName;
    private String siteDescription;
    private String defaultUserRole;
    private Boolean allowRegistration;
    private Boolean maintenanceMode;
    private Boolean emailNotifications;
    private Integer taskReminderDays;
    private Integer maxProjectsPerUser;
    private Integer maxTasksPerProject;
    private Map<String, Object> theme;
    private LocalDateTime updatedAt;
}