package com.taskmanagement.admin.controller;

import com.taskmanagement.admin.model.dto.LogEntryDto;
import com.taskmanagement.admin.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Slf4j
public class LogController {

    private final LogService logService;

    @GetMapping("/sources")
    public ResponseEntity<List<String>> getLogSources() {
        log.info("Request to get log sources");
        return ResponseEntity.ok(logService.getLogSources());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(required = false) Map<String, Object> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        log.info("Request to get logs with filters: {}", filters);
        return ResponseEntity.ok(logService.getLogs(filters, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogEntryDto> getLogById(@PathVariable Long id) {
        log.info("Request to get log with id: {}", id);
        return ResponseEntity.ok(logService.getLogById(id));
    }

    @PostMapping
    public ResponseEntity<LogEntryDto> createLogEntry(@RequestBody LogEntryDto logEntryDto) {
        log.info("Request to create new log entry");
        return ResponseEntity.ok(logService.createLogEntry(logEntryDto));
    }

    @PostMapping("/export")
    public ResponseEntity<Map<String, String>> exportLogs(@RequestBody(required = false) Map<String, Object> filters) {
        log.info("Request to export logs with filters: {}", filters);
        return ResponseEntity.ok(logService.exportLogs(filters != null ? filters : Map.of()));
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearLogs(@RequestBody(required = false) Map<String, Object> filters) {
        log.info("Request to clear logs with filters: {}", filters);
        return ResponseEntity.ok(logService.clearLogs(filters != null ? filters : Map.of()));
    }
}