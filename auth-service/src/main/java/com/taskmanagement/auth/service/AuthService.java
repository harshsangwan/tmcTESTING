package com.taskmanagement.auth.service;

import com.taskmanagement.auth.exception.ResourceNotFoundException;
import com.taskmanagement.auth.model.dto.AuthResponse;
import com.taskmanagement.auth.model.dto.LoginRequest;
import com.taskmanagement.auth.model.dto.RegisterRequest;
import com.taskmanagement.auth.model.entity.Role;
import com.taskmanagement.auth.model.entity.User;
import com.taskmanagement.auth.repository.RoleRepository;
import com.taskmanagement.auth.repository.UserRepository;
import com.taskmanagement.auth.security.JwtUtil;
import com.taskmanagement.auth.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Update last login time
        userRepository.findById(userDetails.getId())
            .ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            });
        
        return AuthResponse.builder()
                .id(userDetails.getId())
                .name(userDetails.getName())
                .email(userDetails.getUsername())
                .role(userDetails.getMainRole())
                .token(jwt)
                .build();
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken!");
        }

        // Create new user
        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .emailVerified(true) // Auto-verify for now
                .accountLocked(false)
                .build();

        // Set role as MEMBER by default
        Set<Role> roles = new HashSet<>();
        Role memberRole = roleRepository.findByName(Role.RoleName.ROLE_MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role MEMBER is not found."));
        roles.add(memberRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Authenticate the newly registered user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);
        
        // Update last login time
        savedUser.setLastLoginAt(LocalDateTime.now());
        userRepository.save(savedUser);

        return AuthResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role("MEMBER") // Default role for new users
                .token(jwt)
                .build();
    }
}