package com.taskmanagement.task.service;

import com.taskmanagement.task.config.TaskSecurity;
import com.taskmanagement.task.config.UserPrincipal;
import com.taskmanagement.task.exception.AccessDeniedException;
import com.taskmanagement.task.exception.ResourceNotFoundException;
import com.taskmanagement.task.mapper.TaskCommentMapper;
import com.taskmanagement.task.model.dto.TaskCommentDto;
import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskComment;
import com.taskmanagement.task.model.entity.TaskHistory.ActionType;
import com.taskmanagement.task.repository.TaskCommentRepository;
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
public class TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskCommentMapper taskCommentMapper;
    private final TaskSecurity taskSecurity;
    private final TaskHistoryService taskHistoryService;

    /**
     * Get all comments for a task
     */
    public List<TaskCommentDto> getTaskComments(Long taskId) {
        // Check if the task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has permission to access this task
        if (!taskSecurity.canAccessTask(taskId)) {
            throw new AccessDeniedException("You don't have permission to view this task's comments");
        }
        
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        
        return comments.stream()
                .map(taskCommentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Add a comment to a task
     */
    @Transactional
    public TaskCommentDto addTaskComment(TaskCommentDto commentDto) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Check if the task exists
        Task task = taskRepository.findById(commentDto.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + commentDto.getTaskId()));
        
        // Check if user has permission to access this task
        if (!taskSecurity.canAccessTask(commentDto.getTaskId())) {
            throw new AccessDeniedException("You don't have permission to comment on this task");
        }
        
        // Set user information
        commentDto.setUserId(currentUser.getId());
        commentDto.setUserName(currentUser.getName());
        commentDto.setUserEmail(currentUser.getEmail());
        
        // Create and save the comment
        TaskComment comment = taskCommentMapper.toEntity(commentDto);
        TaskComment savedComment = taskCommentRepository.save(comment);
        
        // Create task history entry for comment
        taskHistoryService.createTaskHistory(
                task,
                ActionType.COMMENTED,
                null,
                commentDto.getContent(),
                "comment",
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Comment added to task: {}", task.getTitle());
        
        return taskCommentMapper.toDto(savedComment);
    }
    
    /**
     * Update a comment
     */
    @Transactional
    public TaskCommentDto updateTaskComment(Long id, TaskCommentDto commentDto) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Check if the comment exists
        TaskComment comment = taskCommentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        
        // Check if user is the owner of the comment
        if (!comment.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You can only edit your own comments");
        }
        
        // Keep old content for history
        String oldContent = comment.getContent();
        
        // Update only the content
        taskCommentMapper.updateEntity(commentDto, comment);
        
        TaskComment updatedComment = taskCommentRepository.save(comment);
        
        // Create task history entry for comment update
        taskHistoryService.createTaskHistory(
                comment.getTask(),
                ActionType.UPDATED,
                oldContent,
                comment.getContent(),
                "comment",
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Comment updated for task: {}", comment.getTask().getTitle());
        
        return taskCommentMapper.toDto(updatedComment);
    }
    
    /**
     * Delete a comment
     */
    @Transactional
    public void deleteTaskComment(Long id) {
        UserPrincipal currentUser = taskSecurity.getCurrentUser();
        
        // Check if the comment exists
        TaskComment comment = taskCommentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        
        // Check if user is the owner of the comment or an admin
        if (!comment.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        
        // Get the task for history entry
        Task task = comment.getTask();
        
        // Delete the comment
        taskCommentRepository.delete(comment);
        
        // Create task history entry for comment deletion
        taskHistoryService.createTaskHistory(
                task,
                ActionType.UPDATED,
                comment.getContent(),
                "Comment deleted",
                "comment",
                currentUser.getId(),
                currentUser.getName()
        );
        
        log.info("Comment deleted from task: {}", task.getTitle());
    }
}