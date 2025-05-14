package com.taskmanagement.task.service;

import com.taskmanagement.task.config.TaskSecurity;
import com.taskmanagement.task.config.UserPrincipal;
import com.taskmanagement.task.exception.AccessDeniedException;
import com.taskmanagement.task.exception.ResourceNotFoundException;
import com.taskmanagement.task.mapper.TaskHistoryMapper;
import com.taskmanagement.task.model.dto.TaskHistoryDto;
import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskHistory;
import com.taskmanagement.task.model.entity.TaskHistory.ActionType;
import com.taskmanagement.task.repository.TaskHistoryRepository;
import com.taskmanagement.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskHistoryService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskHistoryMapper taskHistoryMapper;
    private final TaskSecurity taskSecurity;

    /**
     * Get history for a task
     */
    public List<TaskHistoryDto> getTaskHistory(Long taskId) {
        // Check if the task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has permission to access this task
        if (!taskSecurity.canAccessTask(taskId)) {
            throw new AccessDeniedException("You don't have permission to view this task's history");
        }
        
        List<TaskHistory> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        
        return history.stream()
                .map(taskHistoryMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a history entry for a task
     */
    @Transactional
    public TaskHistoryDto createTaskHistory(Task task, ActionType actionType, 
                                          String oldValue, String newValue, 
                                          String fieldName, Long userId, String userName) {
        
        TaskHistory history = TaskHistory.builder()
                .task(task)
                .actionType(actionType)
                .oldValue(oldValue)
                .newValue(newValue)
                .fieldName(fieldName)
                .userId(userId)
                .userName(userName)
                .build();
        
        TaskHistory savedHistory = taskHistoryRepository.save(history);
        
        return taskHistoryMapper.toDto(savedHistory);
    }
    
    /**
     * Get all history entries for the current user
     */
    public List<TaskHistoryDto> getUserTaskHistory() {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        List<TaskHistory> history = taskHistoryRepository.findByUserId(currentUser.getId());
        
        return history.stream()
                .map(taskHistoryMapper::toDto)
                .collect(Collectors.toList());
    }
}