package com.taskmanagement.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.admin.exception.ResourceNotFoundException;
import com.taskmanagement.admin.model.dto.LogEntryDto;
import com.taskmanagement.admin.model.entity.LogEntry;
import com.taskmanagement.admin.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogEntryRepository logEntryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get all log sources (categories)
     */
    public List<String> getLogSources() {
        return logEntryRepository.findAllSources();
    }
    
    /**
     * Get logs with pagination and filters
     */
    public Map<String, Object> getLogs(Map<String, Object> filters, int page, int size) {
        // Create pageable
        Pageable pageable = PageRequest.of(page, size);
        
        // Extract filters
        String level = filters.get("level") != null ? (String) filters.get("level") : null;
        String source = filters.get("source") != null ? (String) filters.get("source") : null;
        Long userId = filters.get("userId") != null ? Long.valueOf(filters.get("userId").toString()) : null;
        String search = filters.get("search") != null ? (String) filters.get("search") : null;
        
        LocalDateTime startDate = null;
        if (filters.get("startDate") != null) {
            LocalDate date = LocalDate.parse((String) filters.get("startDate"));
            startDate = date.atStartOfDay();
        }
        
        LocalDateTime endDate = null;
        if (filters.get("endDate") != null) {
            LocalDate date = LocalDate.parse((String) filters.get("endDate"));
            endDate = date.atTime(LocalTime.MAX);
        }
        
        // Query logs with filters
        Page<LogEntry> logsPage = logEntryRepository.findWithFilters(
                level, source, userId, startDate, endDate, search, pageable);
        
        // Convert to DTOs
        List<LogEntryDto> logs = logsPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        // Return result
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", logsPage.getTotalElements());
        result.put("page", logsPage.getNumber());
        result.put("size", logsPage.getSize());
        result.put("totalPages", logsPage.getTotalPages());
        
        return result;
    }
    
    /**
     * Get log entry by ID
     */
    public LogEntryDto getLogById(Long id) {
        LogEntry log = logEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log entry not found with id: " + id));
        
        return mapToDto(log);
    }
    
    /**
     * Create a new log entry
     */
    @Transactional
    public LogEntryDto createLogEntry(LogEntryDto logEntryDto) {
        LogEntry logEntry = LogEntry.builder()
                .level(logEntryDto.getLevel())
                .source(logEntryDto.getSource())
                .message(logEntryDto.getMessage())
                .userId(logEntryDto.getUserId())
                .userName(logEntryDto.getUserName())
                .resourceType(logEntryDto.getResourceType())
                .resourceId(logEntryDto.getResourceId())
                .build();
        
        // Convert metadata to JSON
        try {
            if (logEntryDto.getMetadata() != null) {
                logEntry.setMetadata(objectMapper.writeValueAsString(logEntryDto.getMetadata()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing log metadata", e);
        }
        
        LogEntry savedLogEntry = logEntryRepository.save(logEntry);
        
        return mapToDto(savedLogEntry);
    }
    
    /**
     * Export logs with filters
     */
    public Map<String, String> exportLogs(Map<String, Object> filters) {
        // In a real implementation, this would export logs to a file
        // and return a download URL
        
        return Map.of(
                "status", "success",
                "message", "Logs exported successfully",
                "downloadUrl", "/api/admin/logs/downloads/logs-" + System.currentTimeMillis() + ".csv"
        );
    }
    
    /**
     * Clear logs with filters
     */
    @Transactional
    public Map<String, Object> clearLogs(Map<String, Object> filters) {
        // In a real implementation, this would delete logs based on filters
        
        // Just for simulation, let's assume we deleted 10 logs
        int count = 10;
        
        return Map.of(
                "status", "success",
                "message", count + " logs cleared successfully",
                "count", count
        );
    }
    
    /**
     * Map LogEntry entity to LogEntryDto
     */
    private LogEntryDto mapToDto(LogEntry logEntry) {
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            if (logEntry.getMetadata() != null) {
                metadata = objectMapper.readValue(logEntry.getMetadata(), Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Error deserializing log metadata", e);
        }
        
        return LogEntryDto.builder()
                .id(logEntry.getId())
                .timestamp(logEntry.getTimestamp())
                .level(logEntry.getLevel())
                .source(logEntry.getSource())
                .message(logEntry.getMessage())
                .userId(logEntry.getUserId())
                .userName(logEntry.getUserName())
                .resourceType(logEntry.getResourceType())
                .resourceId(logEntry.getResourceId())
                .metadata(metadata)
                .build();
    }
}