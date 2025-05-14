package com.taskmanagement.task.service;

import com.taskmanagement.task.client.AuthServiceClient;
import com.taskmanagement.task.client.ProjectServiceClient;
import com.taskmanagement.task.config.TaskSecurity;
import com.taskmanagement.task.config.UserPrincipal;
import com.taskmanagement.task.exception.AccessDeniedException;
import com.taskmanagement.task.exception.ResourceNotFoundException;
import com.taskmanagement.task.mapper.TaskMapper;
import com.taskmanagement.task.model.dto.ProjectDto;
import com.taskmanagement.task.model.dto.TaskDto;
import com.taskmanagement.task.model.dto.UserDto;
import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.Task.TaskStatus;
import com.taskmanagement.task.model.entity.TaskHistory;
import com.taskmanagement.task.model.entity.TaskHistory.ActionType;
import com.taskmanagement.task.repository.TaskCommentRepository;
import com.taskmanagement.task.repository.TaskHistoryRepository;
import com.taskmanagement.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskMapper taskMapper;
    private final TaskSecurity taskSecurity;
    private final AuthServiceClient authServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final TaskHistoryService taskHistoryService;

    /**
     * Get all tasks
     */
    public List<TaskDto> getAllTasks() {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        List<Task> tasks;
        
        if (currentUser.isAdmin() || currentUser.isManager()) {
            // Admins and Managers can see all tasks
            tasks = taskRepository.findAll();
        } else {
            // Regular users can only see tasks they created or are assigned to
            tasks = taskRepository.findByCreatedByOrAssignedTo(currentUser.getId(), currentUser.getId());
        }
        
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a task by ID
     */
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to access this task
        if (!taskSecurity.canAccessTask(id)) {
            throw new AccessDeniedException("You don't have permission to view this task");
        }
        
        return taskMapper.toDto(task);
    }
    
    /**
     * Get tasks by project ID
     */
    public List<TaskDto> getTasksByProjectId(Long projectId) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Verify that the project exists by calling project service
        try {
            String authHeader = "Bearer " + currentUser.getToken();
            projectServiceClient.getProjectById(authHeader, projectId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        // If not admin/manager, filter tasks that the user has access to
        if (!currentUser.isAdmin() && !currentUser.isManager()) {
            tasks = tasks.stream()
                    .filter(task -> task.getCreatedBy().equals(currentUser.getId()) || 
                                    (task.getAssignedTo() != null && task.getAssignedTo().equals(currentUser.getId())))
                    .collect(Collectors.toList());
        }
        
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by status
     */
    public List<TaskDto> getTasksByStatus(TaskStatus status) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        List<Task> tasks = taskRepository.findByStatus(status);
        
        // If not admin/manager, filter tasks that the user has access to
        if (!currentUser.isAdmin() && !currentUser.isManager()) {
            tasks = tasks.stream()
                    .filter(task -> task.getCreatedBy().equals(currentUser.getId()) || 
                                    (task.getAssignedTo() != null && task.getAssignedTo().equals(currentUser.getId())))
                    .collect(Collectors.toList());
        }
        
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks assigned to a user
     */
    public List<TaskDto> getTasksByAssignedTo(Long userId) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Users can only see their own assigned tasks unless they are admins/managers
        if (!currentUser.getId().equals(userId) && !currentUser.isAdmin() && !currentUser.isManager()) {
            throw new AccessDeniedException("You don't have permission to view other users' tasks");
        }
        
        List<Task> tasks = taskRepository.findByAssignedTo(userId);
        
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks created by a user
     */
    public List<TaskDto> getTasksByCreatedBy(Long userId) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Users can only see their own created tasks unless they are admins/managers
        if (!currentUser.getId().equals(userId) && !currentUser.isAdmin() && !currentUser.isManager()) {
            throw new AccessDeniedException("You don't have permission to view other users' tasks");
        }
        
        List<Task> tasks = taskRepository.findByCreatedBy(userId);
        
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new task
     */
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Verify that the project exists by calling project service
        ProjectDto project;
        try {
            String authHeader = "Bearer " + currentUser.getToken();
            project = projectServiceClient.getProjectById(authHeader, taskDto.getProjectId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Project not found with id: " + taskDto.getProjectId());
        }
        
        // Set project name from the fetched project
        taskDto.setProjectName(project.getName());
        
        // Set creator information
        taskDto.setCreatedBy(currentUser.getId());
        taskDto.setCreatedByName(currentUser.getName());
        
        // If task is assigned, verify the user exists and set the assignee name
        if (taskDto.getAssignedTo() != null) {
            try {
                String authHeader = "Bearer " + currentUser.getToken();
                UserDto assignee = authServiceClient.getUserById(authHeader, taskDto.getAssignedTo());
                taskDto.setAssigneeName(assignee.getName());
            } catch (Exception e) {
                throw new ResourceNotFoundException("Assigned user not found with id: " + taskDto.getAssignedTo());
            }
        }
        
        // Create and save the task
        Task task = taskMapper.toEntity(taskDto);
        Task savedTask = taskRepository.save(task);
        
        // Create task history entry for task creation
        taskHistoryService.createTaskHistory(
                savedTask,
                ActionType.CREATED,
                null,
                null,
                null,
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Task created successfully: {}", savedTask.getTitle());
        
        return taskMapper.toDto(savedTask);
    }
    
    /**
     * Update a task
     */
    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to update this task
        if (!taskSecurity.canAccessTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        
        // Keep track of old values for history
        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        TaskStatus oldStatus = task.getStatus();
        Task.TaskPriority oldPriority = task.getPriority();
        Long oldAssignedTo = task.getAssignedTo();
        String oldAssigneeName = task.getAssigneeName();
        Long oldProjectId = task.getProjectId();
        
        // If project is being changed, verify the new project exists
        if (taskDto.getProjectId() != null && !taskDto.getProjectId().equals(task.getProjectId())) {
            try {
                String authHeader = "Bearer " + currentUser.getToken();
                ProjectDto project = projectServiceClient.getProjectById(authHeader, taskDto.getProjectId());
                taskDto.setProjectName(project.getName());
            } catch (Exception e) {
                throw new ResourceNotFoundException("Project not found with id: " + taskDto.getProjectId());
            }
        }
        
        // If assignee is being changed, verify the new assignee exists
        if (taskDto.getAssignedTo() != null && !taskDto.getAssignedTo().equals(task.getAssignedTo())) {
            try {
                String authHeader = "Bearer " + currentUser.getToken();
                UserDto assignee = authServiceClient.getUserById(authHeader, taskDto.getAssignedTo());
                taskDto.setAssigneeName(assignee.getName());
            } catch (Exception e) {
                throw new ResourceNotFoundException("Assigned user not found with id: " + taskDto.getAssignedTo());
            }
        }
        
        // Update entity (this will not touch createdBy, createdAt, etc.)
        taskMapper.updateEntity(taskDto, task);
        
        // Set completed_at timestamp if status is changed to DONE
        if (oldStatus != TaskStatus.DONE && task.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (oldStatus == TaskStatus.DONE && task.getStatus() != TaskStatus.DONE) {
            task.setCompletedAt(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        
        // Create task history entries for changes
        if (!oldTitle.equals(updatedTask.getTitle())) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.UPDATED,
                    oldTitle,
                    updatedTask.getTitle(),
                    "title",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        if ((oldDescription == null && updatedTask.getDescription() != null) ||
                (oldDescription != null && !oldDescription.equals(updatedTask.getDescription()))) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.UPDATED,
                    oldDescription,
                    updatedTask.getDescription(),
                    "description",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        if (oldStatus != updatedTask.getStatus()) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.STATUS_CHANGED,
                    oldStatus.toString(),
                    updatedTask.getStatus().toString(),
                    "status",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        if (oldPriority != updatedTask.getPriority()) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.PRIORITY_CHANGED,
                    oldPriority.toString(),
                    updatedTask.getPriority().toString(),
                    "priority",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        // Handle assignment changes
        if ((oldAssignedTo == null && updatedTask.getAssignedTo() != null) ||
                (oldAssignedTo != null && !oldAssignedTo.equals(updatedTask.getAssignedTo()))) {
            if (updatedTask.getAssignedTo() == null) {
                taskHistoryService.createTaskHistory(
                        updatedTask,
                        ActionType.UNASSIGNED,
                        oldAssigneeName,
                        null,
                        "assignee",
                        currentUser.getId(),
                        currentUser.getName()
                );
            } else {
                taskHistoryService.createTaskHistory(
                        updatedTask,
                        ActionType.ASSIGNED,
                        oldAssigneeName,
                        updatedTask.getAssigneeName(),
                        "assignee",
                        currentUser.getId(),
                        currentUser.getName()
                );
            }
        }
        
        // Handle project change
        if (!oldProjectId.equals(updatedTask.getProjectId())) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.UPDATED,
                    oldProjectId.toString(),
                    updatedTask.getProjectId().toString(),
                    "project",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        log.info("Task updated successfully: {}", updatedTask.getTitle());
        
        return taskMapper.toDto(updatedTask);
    }
    
    /**
     * Delete a task
     */
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to delete this task
        if (!taskSecurity.canDeleteTask(id)) {
            throw new AccessDeniedException("You don't have permission to delete this task");
        }
        
        // Delete task comments and history
        taskCommentRepository.deleteByTaskId(id);
        taskHistoryRepository.deleteByTaskId(id);
        
        // Delete the task
        taskRepository.delete(task);
        
        log.info("Task deleted successfully: {}", task.getTitle());
    }
    
    /**
     * Change task status
     */
    @Transactional
    public TaskDto changeTaskStatus(Long id, TaskStatus status) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to update this task
        if (!taskSecurity.canAccessTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        
        // Keep track of old value for history
        TaskStatus oldStatus = task.getStatus();
        
        // Update the status
        task.setStatus(status);
        
        // Set completed_at timestamp if status is changed to DONE
        if (oldStatus != TaskStatus.DONE && status == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (oldStatus == TaskStatus.DONE && status != TaskStatus.DONE) {
            task.setCompletedAt(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        
        // Create task history entry for status change
        taskHistoryService.createTaskHistory(
                updatedTask,
                ActionType.STATUS_CHANGED,
                oldStatus.toString(),
                status.toString(),
                "status",
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Task status updated successfully for {}: {}", task.getTitle(), status);
        
        return taskMapper.toDto(updatedTask);
    }
    
    /**
     * Change task priority
     */
    @Transactional
    public TaskDto changeTaskPriority(Long id, Task.TaskPriority priority) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to update this task
        if (!taskSecurity.canAccessTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        
        // Keep track of old value for history
        Task.TaskPriority oldPriority = task.getPriority();
        
        // Update the priority
        task.setPriority(priority);
        
        Task updatedTask = taskRepository.save(task);
        
        // Create task history entry for priority change
        taskHistoryService.createTaskHistory(
                updatedTask,
                ActionType.PRIORITY_CHANGED,
                oldPriority.toString(),
                priority.toString(),
                "priority",
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Task priority updated successfully for {}: {}", task.getTitle(), priority);
        
        return taskMapper.toDto(updatedTask);
    }
    
    /**
     * Assign task to user
     */
    @Transactional
    public TaskDto assignTask(Long id, Long userId) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Check if user has permission to update this task
        if (!taskSecurity.canAccessTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        }
        
        // Keep track of old values for history
        Long oldAssignedTo = task.getAssignedTo();
        String oldAssigneeName = task.getAssigneeName();
        
        // If assigning to a user, verify the user exists
        String assigneeName = null;
        if (userId != null) {
            try {
                String authHeader = "Bearer " + currentUser.getToken();
                UserDto assignee = authServiceClient.getUserById(authHeader, userId);
                assigneeName = assignee.getName();
            } catch (Exception e) {
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }
        }
        
        // Update assignment
        task.setAssignedTo(userId);
        task.setAssigneeName(assigneeName);
        
        Task updatedTask = taskRepository.save(task);
        
        // Create task history entry for assignment change
        if (userId == null) {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.UNASSIGNED,
                    oldAssigneeName,
                    null,
                    "assignee",
                    currentUser.getId(),
                    currentUser.getName()
            );
        } else {
            taskHistoryService.createTaskHistory(
                    updatedTask,
                    ActionType.ASSIGNED,
                    oldAssigneeName,
                    assigneeName,
                    "assignee",
                    currentUser.getId(),
                    currentUser.getName()
            );
        }
        
        log.info("Task assignment updated successfully for {}: {}", task.getTitle(), assigneeName != null ? assigneeName : "Unassigned");
        
        return taskMapper.toDto(updatedTask);
    }
}