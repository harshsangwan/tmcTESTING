package com.taskmanagement.admin.client;

import com.taskmanagement.admin.model.dto.ProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "project-service", url = "${service.project-service.url}")
public interface ProjectServiceClient {
    
    @GetMapping("/api/projects")
    List<ProjectDto> getAllProjects(@RequestHeader("Authorization") String authHeader);
    
    @GetMapping("/api/projects/{id}")
    ProjectDto getProjectById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @GetMapping("/api/projects/status/{status}")
    List<ProjectDto> getProjectsByStatus(@RequestHeader("Authorization") String authHeader, @PathVariable String status);
    
    @GetMapping("/api/projects/upcoming")
    List<ProjectDto> getUpcomingProjects(@RequestHeader("Authorization") String authHeader, @RequestParam(defaultValue = "7") int days);
}