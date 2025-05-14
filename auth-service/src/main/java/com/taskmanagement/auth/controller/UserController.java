package com.taskmanagement.auth.controller;

import com.taskmanagement.auth.model.dto.UserDto;
import com.taskmanagement.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Request to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Request to get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        log.info("Request to get user by email: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody Map<String, Object> request) {
        log.info("Request to create user with email: {}", request.get("email"));
        
        UserDto userDto = UserDto.builder()
                .name((String) request.get("name"))
                .email((String) request.get("email"))
                .role((String) request.get("role"))
                .build();
        
        String password = (String) request.get("password");
        
        return ResponseEntity.ok(userService.createUser(userDto, password));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        log.info("Request to update user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        log.info("Request to change role of user with id: {} to {}", id, role);
        return ResponseEntity.ok(userService.changeUserRole(id, role));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        log.info("Request to reset password for user with id: {}", id);
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok().body(Map.of("message", "Password reset successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Request to delete user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok().body(Map.of("message", "User deleted successfully"));
    }
}