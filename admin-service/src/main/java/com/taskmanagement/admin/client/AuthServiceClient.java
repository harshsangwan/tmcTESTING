package com.taskmanagement.admin.client;

import com.taskmanagement.admin.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service", url = "${service.auth-service.url}")
public interface AuthServiceClient {
    
    @GetMapping("/api/users")
    List<UserDto> getAllUsers(@RequestHeader("Authorization") String authHeader);
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @PostMapping("/api/users")
    UserDto createUser(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> userRequest);
    
    @PutMapping("/api/users/{id}")
    UserDto updateUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody UserDto userDto);
    
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@RequestHeader("Authorization") String authHeader, @PathVariable Long id);
    
    @PatchMapping("/api/users/{id}/role")
    UserDto changeUserRole(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Map<String, String> roleRequest);
    
    @PostMapping("/api/users/{id}/reset-password")
    Map<String, String> resetPassword(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Map<String, String> passwordRequest);
}