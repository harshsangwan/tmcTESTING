package com.taskmanagement.admin.controller;

import com.taskmanagement.admin.model.dto.SystemSettingsDto;
import com.taskmanagement.admin.service.SystemSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping
    public ResponseEntity<SystemSettingsDto> getSettings() {
        log.info("Request to get system settings");
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SystemSettingsDto> updateSettings(@Valid @RequestBody SystemSettingsDto settingsDto) {
        log.info("Request to update system settings");
        return ResponseEntity.ok(systemSettingsService.updateSettings(settingsDto));
    }

    @PostMapping("/backup")
    public ResponseEntity<Map<String, String>> backupSystem() {
        log.info("Request to create system backup");
        return ResponseEntity.ok(systemSettingsService.backupSystem());
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restoreSystem() {
        log.info("Request to restore system from backup");
        return ResponseEntity.ok(systemSettingsService.restoreSystem());
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        log.info("Request to clear system cache");
        return ResponseEntity.ok(systemSettingsService.clearCache());
    }
}