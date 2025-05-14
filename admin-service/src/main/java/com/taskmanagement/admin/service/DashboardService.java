package com.taskmanagement.admin.service;

import com.taskmanagement.admin.client.AuthServiceClient;
import com.taskmanagement.admin.client.ProjectServiceClient;
import com.taskmanagement.admin.client.TaskServiceClient;
import com.taskmanagement.admin.config.UserPrincipal;
import com.taskmanagement.admin.model.dto.ProjectDto;
import com.taskmanagement.admin.model.dto.TaskDto;
import com.taskmanagement.admin.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AuthServiceClient authServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final TaskServiceClient taskServiceClient;

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats(UserPrincipal currentUser) {
        String authHeader = "Bearer " + currentUser.getToken();
        
        // Fetch data from services
        List<UserDto> users = authServiceClient.getAllUsers(authHeader);
        List<ProjectDto> projects = projectServiceClient.getAllProjects(authHeader);
        List<TaskDto> tasks = taskServiceClient.getAllTasks(authHeader);
        
        Map<String, Object> stats = new HashMap<>();
        
        // User stats
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", users.size());
        
        Map<String, Long> usersByRole = users.stream()
                .collect(Collectors.groupingBy(UserDto::getRole, Collectors.counting()));
        userStats.put("byRole", usersByRole);
        
        // Project stats
        Map<String, Object> projectStats = new HashMap<>();
        projectStats.put("total", projects.size());
        
        Map<String, Long> projectsByStatus = projects.stream()
                .collect(Collectors.groupingBy(ProjectDto::getStatus, Collectors.counting()));
        projectStats.put("byStatus", projectsByStatus);
        
        // Calculate average project completion
        double avgProgress = projects.stream()
                .mapToInt(ProjectDto::getProgress)
                .average()
                .orElse(0.0);
        projectStats.put("avgProgress", avgProgress);
        
        // Upcoming projects (due in next 30 days)
        List<ProjectDto> upcomingProjects = projectServiceClient.getUpcomingProjects(authHeader, 30);
        projectStats.put("upcoming", upcomingProjects.size());
        
        // Task stats
        Map<String, Object> taskStats = new HashMap<>();
        taskStats.put("total", tasks.size());
        
        Map<String, Long> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(TaskDto::getStatus, Collectors.counting()));
        taskStats.put("byStatus", tasksByStatus);
        
        Map<String, Long> tasksByPriority = tasks.stream()
                .collect(Collectors.groupingBy(TaskDto::getPriority, Collectors.counting()));
        taskStats.put("byPriority", tasksByPriority);
        
        // Tasks due soon (in next 7 days)
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        long tasksDueSoon = tasks.stream()
                .filter(task -> task.getDueDate() != null && 
                        !task.getDueDate().isAfter(nextWeek) && 
                        !task.getStatus().equals("DONE"))
                .count();
        taskStats.put("dueSoon", tasksDueSoon);
        
        // Overdue tasks
        LocalDate today = LocalDate.now();
        long overdueTasksCount = tasks.stream()
                .filter(task -> task.getDueDate() != null && 
                        task.getDueDate().isBefore(today) && 
                        !task.getStatus().equals("DONE"))
                .count();
        taskStats.put("overdue", overdueTasksCount);
        
        // System status (just some sample data)
        Map<String, Object> systemStatus = new HashMap<>();
        systemStatus.put("maintenanceMode", false);
        systemStatus.put("lastBackup", LocalDate.now().minusDays(1).toString());
        systemStatus.put("uptime", "14 days");
        
        // Combine all stats
        stats.put("userStats", userStats);
        stats.put("projectStats", projectStats);
        stats.put("taskStats", taskStats);
        stats.put("systemStatus", systemStatus);
        
        return stats;
    }
    
    /**
     * Get recent activity data
     */
    public List<Map<String, Object>> getRecentActivity(UserPrincipal currentUser) {
        // In a real implementation, this would fetch recent activity logs
        // For now, just return some sample data
        
        List<Map<String, Object>> activities = List.of(
            Map.of(
                "type", "USER",
                "action", "CREATED",
                "userName", "John Doe",
                "targetName", "New User",
                "timestamp", LocalDate.now().atStartOfDay()
            ),
            Map.of(
                "type", "PROJECT",
                "action", "UPDATED",
                "userName", "Jane Smith",
                "targetName", "Website Redesign",
                "timestamp", LocalDate.now().atStartOfDay().minusHours(2)
            ),
            Map.of(
                "type", "TASK",
                "action", "COMPLETED",
                "userName", "Bob Johnson",
                "targetName", "Design Homepage",
                "timestamp", LocalDate.now().atStartOfDay().minusHours(5)
            ),
            Map.of(
                "type", "SYSTEM",
                "action", "BACKUP",
                "userName", "System",
                "targetName", "Daily Backup",
                "timestamp", LocalDate.now().atStartOfDay().minusHours(12)
            )
        );
        
        return activities;
    }
}