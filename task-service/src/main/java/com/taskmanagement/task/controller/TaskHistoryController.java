package com.taskmanagement.task.controller;

import com.taskmanagement.task.model.dto.TaskHistoryDto;
import com.taskmanagement.task.service.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<TaskHistoryDto>> getTaskHistory(@PathVariable Long taskId) {
        log.info("Request to get history for task with id: {}", taskId);
        return ResponseEntity.ok(taskHistoryService.getTaskHistory(taskId));
    }

    @GetMapping("/history/user")
    public ResponseEntity<List<TaskHistoryDto>> getUserTaskHistory() {
        log.info("Request to get task history for current user");
        return ResponseEntity.ok(taskHistoryService.getUserTaskHistory());
    }
}