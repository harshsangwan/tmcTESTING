package com.taskmanagement.task.config;

import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;

    /**
     * Check if the current user is the creator of the task
     */
    public boolean isTaskCreator(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return false;
        }
        
        return userPrincipal.getId().equals(task.get().getCreatedBy());
    }
    
    /**
     * Check if the current user is assigned to the task
     */
    public boolean isAssignedToTask(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return false;
        }
        
        return userPrincipal.getId().equals(task.get().getAssignedTo());
    }
    
    /**
     * Check if the user has permission to edit/view a task
     * (Admin, Manager, Creator, or Assignee)
     */
    public boolean canAccessTask(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        // Admin or Manager can access any task
        if (userPrincipal.isAdmin() || userPrincipal.isManager()) {
            return true;
        }
        
        // Otherwise, check if the user is the creator or assignee
        return isTaskCreator(taskId) || isAssignedToTask(taskId);
    }
    
    /**
     * Check if the user has permission to delete a task
     * (Admin or Creator)
     */
    public boolean canDeleteTask(Long taskId) {
        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            return false;
        }
        
        // Admin can delete any task
        if (userPrincipal.isAdmin()) {
            return true;
        }
        
        // Otherwise, check if the user is the creator
        return isTaskCreator(taskId);
    }
    
    /**
     * Get the current authenticated user
     */
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        
        return (UserPrincipal) authentication.getPrincipal();
    }
}