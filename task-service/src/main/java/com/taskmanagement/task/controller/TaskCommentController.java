package com.taskmanagement.task.controller;

import com.taskmanagement.task.model.dto.TaskCommentDto;
import com.taskmanagement.task.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentDto>> getTaskComments(@PathVariable Long taskId) {
        log.info("Request to get comments for task with id: {}", taskId);
        return ResponseEntity.ok(taskCommentService.getTaskComments(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentDto> addTaskComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentDto commentDto) {
        
        // Ensure the task ID in the path matches the one in the DTO
        commentDto.setTaskId(taskId);
        
        log.info("Request to add comment to task with id: {}", taskId);
        return ResponseEntity.ok(taskCommentService.addTaskComment(commentDto));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<TaskCommentDto> updateTaskComment(
            @PathVariable Long id,
            @Valid @RequestBody TaskCommentDto commentDto) {
        
        log.info("Request to update comment with id: {}", id);
        return ResponseEntity.ok(taskCommentService.updateTaskComment(id, commentDto));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteTaskComment(@PathVariable Long id) {
        log.info("Request to delete comment with id: {}", id);
        taskCommentService.deleteTaskComment(id);
        return ResponseEntity.ok().build();
    }
}