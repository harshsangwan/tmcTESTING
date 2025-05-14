package com.taskmanagement.task.client;

import com.taskmanagement.task.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${service.auth-service.url}")
public interface AuthServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@RequestHeader("Authorization") String authHeader, @PathVariable String email);
}