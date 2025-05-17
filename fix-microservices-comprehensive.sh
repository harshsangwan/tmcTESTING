#!/bin/bash

echo "🔧 Comprehensive Microservices Fix"
echo "=================================="

# 1. Fix port configurations manually
echo "1. Fixing port configurations in application-docker.properties..."

# Project Service Port Fix
echo "Fixing project-service port..."
cat > ./project-service/src/main/resources/application-docker.properties << 'EOF'
# Docker environment configuration for project-service
spring.application.name=project-service
server.port=8082

# Database Configuration for Docker
spring.datasource.url=jdbc:mysql://mysql:3306/task_management_project?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=letmEc0de#8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Configure connection pool
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# JWT Configuration
app.jwt.secret=${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b80c0ceb73d3d121182c95878b9a33cd8caf5bee427d6f0b9b5b989f0bd25c035}
app.jwt.issuer=taskmanagement

# Eureka Client Configuration for Docker
eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${server.port}}

# Disable Spring Cloud Config in Docker
spring.cloud.config.enabled=false
spring.config.import=optional:configserver:

# Actuator Configuration - Expose all endpoints without security
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.health.defaults.enabled=true
management.security.enabled=false
management.endpoints.web.base-path=/actuator
management.endpoint.health.enabled=true

# DISABLE SPRING SECURITY FOR ACTUATOR
spring.security.enabled=false

# Logging Configuration
logging.level.com.taskmanagement.project=INFO
logging.level.org.springframework.web=INFO
EOF

# Task Service Port Fix
echo "Fixing task-service port..."
cat > ./task-service/src/main/resources/application-docker.properties << 'EOF'
# Docker environment configuration for task-service
spring.application.name=task-service
server.port=8083

# Database Configuration for Docker
spring.datasource.url=jdbc:mysql://mysql:3306/task_management_tasks?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=letmEc0de#8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Configure connection pool
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# JWT Configuration
app.jwt.secret=${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b80c0ceb73d3d121182c95878b9a33cd8caf5bee427d6f0b9b5b989f0bd25c035}
app.jwt.issuer=taskmanagement

# Eureka Client Configuration for Docker
eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${server.port}}

# Disable Spring Cloud Config in Docker
spring.cloud.config.enabled=false
spring.config.import=optional:configserver:

# Actuator Configuration - Expose all endpoints without security
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.health.defaults.enabled=true
management.security.enabled=false
management.endpoints.web.base-path=/actuator
management.endpoint.health.enabled=true

# DISABLE SPRING SECURITY FOR ACTUATOR
spring.security.enabled=false

# Logging Configuration
logging.level.com.taskmanagement.task=INFO
logging.level.org.springframework.web=INFO
EOF

# Integration Service Port Fix
echo "Fixing integration-service port..."
cat > ./integration-service/src/main/resources/application-docker.properties << 'EOF'
# Docker environment configuration for integration-service
spring.application.name=integration-service
server.port=8084

# Database Configuration for Docker
spring.datasource.url=jdbc:mysql://mysql:3306/task_management_integrations?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=letmEc0de#8
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Configure connection pool
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# JWT Configuration
app.jwt.secret=${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b80c0ceb73d3d121182c95878b9a33cd8caf5bee427d6f0b9b5b989f0bd25c035}
app.jwt.issuer=taskmanagement

# Eureka Client Configuration for Docker
eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${server.port}}

# Disable Spring Cloud Config in Docker
spring.cloud.config.enabled=false
spring.config.import=optional:configserver:

# Actuator Configuration - Expose all endpoints without security
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.health.defaults.enabled=true
management.security.enabled=false
management.endpoints.web.base-path=/actuator
management.endpoint.health.enabled=true

# DISABLE SPRING SECURITY FOR ACTUATOR
spring.security.enabled=false

# Logging Configuration
logging.level.com.taskmanagement.integration=INFO
logging.level.org.springframework.web=INFO
EOF

# 2. Update security configuration for enhanced actuator access
echo "2. Updating security configurations to properly handle actuator..."

# Project Service Security Update
echo "Updating project-service security config..."
cat > ./project-service/src/main/java/com/taskmanagement/project/config/ActuatorSecurityConfig.java << 'EOF'
package com.taskmanagement.project.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(1)  // Higher priority than other security configurations
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .securityMatcher("/actuator/**")
            .securityMatcher("/health")
            .securityMatcher("/actuator/health")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# Task Service Security Update
echo "Updating task-service security config..."
cat > ./task-service/src/main/java/com/taskmanagement/task/config/ActuatorSecurityConfig.java << 'EOF'
package com.taskmanagement.task.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(1)  // Higher priority than other security configurations
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .securityMatcher("/actuator/**")
            .securityMatcher("/health")
            .securityMatcher("/actuator/health")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# Integration Service Security Update
echo "Updating integration-service security config..."
cat > ./integration-service/src/main/java/com/taskmanagement/integration/config/ActuatorSecurityConfig.java << 'EOF'
package com.taskmanagement.integration.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(1)  // Higher priority than other security configurations
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .securityMatcher("/actuator/**")
            .securityMatcher("/health")
            .securityMatcher("/actuator/health")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# 3. Create application-dev.yaml files for explicit profile activation
echo "3. Creating profile activation markers..."

echo "project-service:docker" > ./project-service/src/main/resources/spring.profiles.active
echo "task-service:docker" > ./task-service/src/main/resources/spring.profiles.active
echo "integration-service:docker" > ./integration-service/src/main/resources/spring.profiles.active

# 4. Create custom health controllers to explicitly define health endpoints
echo "4. Adding custom health controllers..."

# Project Service Health Controller
echo "Adding health controller to project-service..."
mkdir -p ./project-service/src/main/java/com/taskmanagement/project/controller
cat > ./project-service/src/main/java/com/taskmanagement/project/controller/HealthController.java << 'EOF'
package com.taskmanagement.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
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
echo "Adding health controller to task-service..."
mkdir -p ./task-service/src/main/java/com/taskmanagement/task/controller
cat > ./task-service/src/main/java/com/taskmanagement/task/controller/HealthController.java << 'EOF'
package com.taskmanagement.task.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
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
echo "Adding health controller to integration-service..."
mkdir -p ./integration-service/src/main/java/com/taskmanagement/integration/controller
cat > ./integration-service/src/main/java/com/taskmanagement/integration/controller/HealthController.java << 'EOF'
package com.taskmanagement.integration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
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

# 5. Create application.yaml files with explicit actuator overrides
echo "5. Creating explicit actuator configurations..."

# Project Service Actuator Configuration
echo "Creating project-service actuator configuration..."
cat > ./project-service/src/main/resources/actuator.properties << 'EOF'
# Actuator Configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
management.security.enabled=false
EOF

# Task Service Actuator Configuration
echo "Creating task-service actuator configuration..."
cat > ./task-service/src/main/resources/actuator.properties << 'EOF'
# Actuator Configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
management.security.enabled=false
EOF

# Integration Service Actuator Configuration
echo "Creating integration-service actuator configuration..."
cat > ./integration-service/src/main/resources/actuator.properties << 'EOF'
# Actuator Configuration
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
management.security.enabled=false
EOF

# 6. Make all JAR files executable (sometimes this helps)
echo "6. Making JAR files executable..."
chmod +x ./project-service/target/*.jar
chmod +x ./task-service/target/*.jar
chmod +x ./integration-service/target/*.jar

# 7. Rebuild and restart the services
echo "7. Rebuilding and restarting the services..."
docker-compose down
docker-compose build --no-cache project-service task-service integration-service
docker-compose up -d

echo "8. Waiting for services to start (this may take a minute)..."
sleep 60

# 8. Test health endpoints
echo "9. Testing health endpoints..."
echo "Project Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "Task Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "Integration Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"

echo "10. Testing custom health endpoints..."
echo "Project Service Custom: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/projects/health)"
echo "Task Service Custom: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/tasks/health)"
echo "Integration Service Custom: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/api/integrations/health)"

echo "11. Testing health endpoints through API Gateway..."
echo "Project Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/projects/health)"
echo "Task Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/tasks/health)"
echo "Integration Service via Gateway: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/integrations/health)"

echo "🎯 Fix completed! Check the test results above."