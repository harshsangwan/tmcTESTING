#!/bin/bash

echo "🔧 Fixing Microservices Configuration"
echo "===================================="

# 1. Fix incorrect port configurations
echo "1. Fixing port configurations in application-docker.properties..."
sed -i 's/server.port=808service/server.port=8082/' ./project-service/src/main/resources/application-docker.properties
sed -i 's/server.port=808service/server.port=8083/' ./task-service/src/main/resources/application-docker.properties
sed -i 's/server.port=808service/server.port=8084/' ./integration-service/src/main/resources/application-docker.properties

# 2. Create security configurations for actuator endpoints
echo "2. Creating security configurations for actuator endpoints..."

# For project-service
mkdir -p ./project-service/src/main/java/com/taskmanagement/project/config
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
@Order(1)
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# For task-service
mkdir -p ./task-service/src/main/java/com/taskmanagement/task/config
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
@Order(1)
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# For integration-service
mkdir -p ./integration-service/src/main/java/com/taskmanagement/integration/config
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
@Order(1)
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
EOF

# 3. Rebuild and restart the services
echo "3. Rebuilding and restarting the services..."
docker-compose down
docker-compose build --no-cache project-service task-service integration-service
docker-compose up -d

echo "4. Waiting for services to start (this may take a minute)..."
sleep 60

# 4. Test health endpoints
echo "5. Testing health endpoints..."
echo "Project Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)"
echo "Task Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health)"
echo "Integration Service: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/actuator/health)"

echo "🎯 Fix completed! Check the test results above."