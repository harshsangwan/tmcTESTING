package com.taskmanagement.auth.config;

import com.taskmanagement.auth.model.entity.Role;
import com.taskmanagement.auth.model.entity.User;
import com.taskmanagement.auth.repository.RoleRepository;
import com.taskmanagement.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initUsers();
    }

    private void initRoles() {
        // Create roles if they don't exist
        if (roleRepository.count() == 0) {
            for (Role.RoleName roleName : Role.RoleName.values()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void initUsers() {
        // Create an admin user if no users exist
        if (userRepository.count() == 0) {
            // Get admin role
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));
            
            // Create roles set with admin role
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            
            // Create admin user
            User adminUser = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(roles)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            
            userRepository.save(adminUser);
            log.info("Created admin user: {}", adminUser.getEmail());
            
            // Create a manager user
            Role managerRole = roleRepository.findByName(Role.RoleName.ROLE_MANAGER)
                    .orElseThrow(() -> new RuntimeException("Error: Manager Role not found."));
            
            Set<Role> managerRoles = new HashSet<>();
            managerRoles.add(managerRole);
            
            User managerUser = User.builder()
                    .name("Manager User")
                    .email("manager@example.com")
                    .password(passwordEncoder.encode("manager123"))
                    .roles(managerRoles)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            
            userRepository.save(managerUser);
            log.info("Created manager user: {}", managerUser.getEmail());
            
            // Create a member user
            Role memberRole = roleRepository.findByName(Role.RoleName.ROLE_MEMBER)
                    .orElseThrow(() -> new RuntimeException("Error: Member Role not found."));
            
            Set<Role> memberRoles = new HashSet<>();
            memberRoles.add(memberRole);
            
            User memberUser = User.builder()
                    .name("Member User")
                    .email("member@example.com")
                    .password(passwordEncoder.encode("member123"))
                    .roles(memberRoles)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            
            userRepository.save(memberUser);
            log.info("Created member user: {}", memberUser.getEmail());
        }
    }
}