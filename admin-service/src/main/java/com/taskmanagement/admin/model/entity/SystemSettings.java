package com.taskmanagement.admin.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_description", columnDefinition = "TEXT")
    private String siteDescription;

    @Column(name = "default_user_role")
    private String defaultUserRole;

    @Column(name = "allow_registration")
    private Boolean allowRegistration;

    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode;

    @Column(name = "email_notifications")
    private Boolean emailNotifications;

    @Column(name = "task_reminder_days")
    private Integer taskReminderDays;

    @Column(name = "max_projects_per_user")
    private Integer maxProjectsPerUser;

    @Column(name = "max_tasks_per_project")
    private Integer maxTasksPerProject;

    @Column(name = "theme_settings", columnDefinition = "TEXT")
    private String themeSettings;  // Stored as JSON

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}