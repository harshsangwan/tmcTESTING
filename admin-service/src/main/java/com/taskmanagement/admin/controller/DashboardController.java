package com.taskmanagement.admin.controller;

import com.taskmanagement.admin.config.UserPrincipal;
import com.taskmanagement.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to get dashboard statistics");
        return ResponseEntity.ok(dashboardService.getDashboardStats(currentUser));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to get recent activity");
        return ResponseEntity.ok(dashboardService.getRecentActivity(currentUser));
    }
}