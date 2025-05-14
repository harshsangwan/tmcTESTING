package com.taskmanagement.integration.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String token; // Added token field for service-to-service communication

    @Override
    public String getName() {
        return email;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role) || isAdmin();
    }
    
    public boolean isMember() {
        return "MEMBER".equalsIgnoreCase(role) || isManager();
    }
}