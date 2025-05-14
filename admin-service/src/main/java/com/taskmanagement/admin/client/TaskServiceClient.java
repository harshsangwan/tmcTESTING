package com.taskmanagement.admin.client;

import com.taskmanagement.admin.model.dto.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "task-service", url = "${service.task-service.url}")
public interface TaskServiceClient {
    
    @GetMapping("/api/tasks")
    List<TaskDto> getAllTasks(@RequestHeader("Authorization") String authHeader);
    
    @GetMapping("/api/tasks/{id}")
    TaskDto getTaskById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @GetMapping("/api/tasks/project/{projectId}")
    List<TaskDto> getTasksByProjectId(@RequestHeader("Authorization") String authHeader, @PathVariable Long projectId);
    
    @GetMapping("/api/tasks/status/{status}")
    List<TaskDto> getTasksByStatus(@RequestHeader("Authorization") String authHeader, @PathVariable String status);
    
    @GetMapping("/api/tasks/assigned/{userId}")
    List<TaskDto> getTasksByAssignedUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long userId);
}