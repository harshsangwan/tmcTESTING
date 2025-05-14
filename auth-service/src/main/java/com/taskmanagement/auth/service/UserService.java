package com.taskmanagement.auth.service;

import com.taskmanagement.auth.exception.ResourceNotFoundException;
import com.taskmanagement.auth.model.dto.UserDto;
import com.taskmanagement.auth.model.entity.Role;
import com.taskmanagement.auth.model.entity.User;
import com.taskmanagement.auth.repository.RoleRepository;
import com.taskmanagement.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return mapToDto(user);
    }
    
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return mapToDto(user);
    }
    
    @Transactional
    public UserDto createUser(UserDto userDto, String password) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email is already taken!");
        }
        
        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(password))
                .emailVerified(true) // Auto-verify for now
                .accountLocked(false)
                .build();
        
        // Set role
        Set<Role> roles = new HashSet<>();
        Role.RoleName roleName;
        
        try {
            roleName = Role.RoleName.valueOf("ROLE_" + userDto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role: {}. Setting to MEMBER.", userDto.getRole());
            roleName = Role.RoleName.ROLE_MEMBER;
        }
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found."));
        
        roles.add(role);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());
        
        return mapToDto(savedUser);
    }
    
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setName(userDto.getName());
        
        // Check if email is being changed and is not already taken
        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email is already taken!");
        }
        
        user.setEmail(userDto.getEmail());
        
        // Only update account status fields if they are provided
        if (userDto.getEmailVerified() != null) {
            user.setEmailVerified(userDto.getEmailVerified());
        }
        
        if (userDto.getAccountLocked() != null) {
            user.setAccountLocked(userDto.getAccountLocked());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());
        
        return mapToDto(updatedUser);
    }
    
    @Transactional
    public UserDto changeUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Clear current roles
        user.getRoles().clear();
        
        // Set new role
        Role.RoleName roleName;
        
        try {
            roleName = Role.RoleName.valueOf("ROLE_" + role.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role: {}. Setting to MEMBER.", role);
            roleName = Role.RoleName.ROLE_MEMBER;
        }
        
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found."));
        
        user.getRoles().add(newRole);
        
        User updatedUser = userRepository.save(user);
        log.info("User role changed successfully for {}: {}", updatedUser.getEmail(), role);
        
        return mapToDto(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Don't allow deletion of admin users
        if (user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Admin users cannot be deleted");
        }
        
        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }
    
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    private UserDto mapToDto(User user) {
        String roleName = user.getRoles().stream()
                .map(role -> role.getName().name().replace("ROLE_", ""))
                .findFirst()
                .orElse("MEMBER");
        
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(roleName)
                .emailVerified(user.getEmailVerified())
                .accountLocked(user.getAccountLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}