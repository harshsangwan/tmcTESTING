package com.taskmanagement.task.controller;

import com.taskmanagement.task.model.dto.TaskDto;
import com.taskmanagement.task.model.entity.Task.TaskStatus;
import com.taskmanagement.task.model.entity.Task.TaskPriority;
import com.taskmanagement.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        log.info("Request to get all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        log.info("Request to get task by id: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        log.info("Request to create new task: {}", taskDto.getTitle());
        return ResponseEntity.ok(taskService.createTask(taskDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDto taskDto) {
        log.info("Request to update task with id: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Request to delete task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByProjectId(@PathVariable Long projectId) {
        log.info("Request to get tasks for project: {}", projectId);
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(@PathVariable String status) {
        try {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
            log.info("Request to get tasks with status: {}", taskStatus);
            return ResponseEntity.ok(taskService.getTasksByStatus(taskStatus));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksByAssignedTo(@PathVariable Long userId) {
        log.info("Request to get tasks assigned to user: {}", userId);
        return ResponseEntity.ok(taskService.getTasksByAssignedTo(userId));
    }

    @GetMapping("/created/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksByCreatedBy(@PathVariable Long userId) {
        log.info("Request to get tasks created by user: {}", userId);
        return ResponseEntity.ok(taskService.getTasksByCreatedBy(userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        try {
            TaskStatus status = TaskStatus.valueOf(statusMap.get("status").toUpperCase());
            log.info("Request to update status of task with id: {} to {}", id, status);
            return ResponseEntity.ok(taskService.changeTaskStatus(id, status));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", statusMap.get("status"));
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<TaskDto> updateTaskPriority(@PathVariable Long id, @RequestBody Map<String, String> priorityMap) {
        try {
            TaskPriority priority = TaskPriority.valueOf(priorityMap.get("priority").toUpperCase());
            log.info("Request to update priority of task with id: {} to {}", id, priority);
            return ResponseEntity.ok(taskService.changeTaskPriority(id, priority));
        } catch (IllegalArgumentException e) {
            log.error("Invalid priority: {}", priorityMap.get("priority"));
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TaskDto> assignTask(@PathVariable Long id, @RequestBody Map<String, Long> assignMap) {
        Long userId = assignMap.get("userId");
        log.info("Request to assign task with id: {} to user: {}", id, userId);
        return ResponseEntity.ok(taskService.assignTask(id, userId));
    }
}