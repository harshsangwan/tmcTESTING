package com.taskmanagement.auth.repository;

import com.taskmanagement.auth.model.entity.Role;
import com.taskmanagement.auth.model.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleName name);
}