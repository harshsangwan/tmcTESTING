package com.taskmanagement.task.client;

import com.taskmanagement.task.model.dto.ProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "project-service", url = "${service.project-service.url}")
public interface ProjectServiceClient {
    
    @GetMapping("/api/projects/{id}")
    ProjectDto getProjectById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @GetMapping("/api/projects/user/{userId}")
    ProjectDto[] getProjectsByUserId(@RequestHeader("Authorization") String authHeader, @PathVariable Long userId);
}