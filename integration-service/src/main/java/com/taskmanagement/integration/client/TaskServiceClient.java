package com.taskmanagement.integration.client;

import com.taskmanagement.integration.model.dto.TaskDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "task-service", url = "${service.task-service.url}")
public interface TaskServiceClient {
    
    @GetMapping("/api/tasks/assigned/{userId}")
    List<TaskDto> getTasksForUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long userId);
    
    @GetMapping("/api/tasks/project/{projectId}")
    List<TaskDto> getTasksForProject(@RequestHeader("Authorization") String authHeader, @PathVariable Long projectId);
}