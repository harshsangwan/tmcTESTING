package com.taskmanagement.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    private final LocalDateTime startupTime = LocalDateTime.now();

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("startupTime", startupTime.toString());
        response.put("uptime", calculateUptime());
        
        log.debug("Health check request received");
        return Mono.just(ResponseEntity.ok(response));
    }
    
    @GetMapping("/details")
    public Mono<ResponseEntity<Map<String, Object>>> healthDetails() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("startupTime", startupTime.toString());
        response.put("uptime", calculateUptime());
        
        // Add JVM memory info
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", Runtime.getRuntime().totalMemory());
        memory.put("free", Runtime.getRuntime().freeMemory());
        memory.put("max", Runtime.getRuntime().maxMemory());
        response.put("memory", memory);
        
        // Add processor info
        response.put("processors", Runtime.getRuntime().availableProcessors());
        
        // Add Java info
        Map<String, String> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        response.put("java", java);
        
        // Add OS info
        Map<String, String> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        response.put("os", os);
        
        log.debug("Detailed health check request received");
        return Mono.just(ResponseEntity.ok(response));
    }
    
    private String calculateUptime() {
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startupTime, now);
        long hoursDiff = java.time.temporal.ChronoUnit.HOURS.between(startupTime, now) % 24;
        long minutesDiff = java.time.temporal.ChronoUnit.MINUTES.between(startupTime, now) % 60;
        long secondsDiff = java.time.temporal.ChronoUnit.SECONDS.between(startupTime, now) % 60;
        
        return String.format("%d days, %d hours, %d minutes, %d seconds", 
                daysDiff, hoursDiff, minutesDiff, secondsDiff);
    }
}