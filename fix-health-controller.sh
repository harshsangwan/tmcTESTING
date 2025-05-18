#!/bin/bash
echo "=== Fixing HealthController Package Issue ==="

# Create correct HealthController with proper package
cat > auth-service/src/main/java/com/taskmanagement/auth/controller/HealthController.java << 'EOF'
package com.taskmanagement.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "auth-service");
        return ResponseEntity.ok(status);
    }
}
EOF

echo "Health Controller fixed. Now restart the Auth Service..."