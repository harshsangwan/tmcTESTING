#!/bin/bash

echo "🔧 Backend Fix with Correct Package Structure"
echo "==========================================="

# Create health controller classes with the CORRECT package structure
echo "1. Creating health controllers with correct package structure..."

# Project Service Health Controller
mkdir -p ./project-service/src/main/java/main/java/com/taskmanagement/project/controller
cat > ./project-service/src/main/java/main/java/com/taskmanagement/project/controller/HealthController.java << 'EOF'
package main.java.com.taskmanagement.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "project-service");
        return ResponseEntity.ok(health);
    }
}
EOF

# Task Service Health Controller
mkdir -p ./task-service/src/main/java/main/java/com/taskmanagement/task/controller
cat > ./task-service/src/main/java/main/java/com/taskmanagement/task/controller/HealthController.java << 'EOF'
package main.java.com.taskmanagement.task.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "task-service");
        return ResponseEntity.ok(health);
    }
}
EOF

# Integration Service Health Controller
mkdir -p ./integration-service/src/main/java/main/java/com/taskmanagement/integration/controller
cat > ./integration-service/src/main/java/main/java/com/taskmanagement/integration/controller/HealthController.java << 'EOF'
package main.java.com.taskmanagement.integration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "integration-service");
        return ResponseEntity.ok(health);
    }
}
EOF

echo "2. Rebuilding backend services..."
docker-compose build --no-cache project-service task-service integration-service
docker-compose up -d project-service task-service integration-service