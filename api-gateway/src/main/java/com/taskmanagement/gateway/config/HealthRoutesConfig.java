package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HealthRoutesConfig {
    
    @Bean
    @Primary
    public RouteLocator healthRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring direct health routes");
        
        return builder.routes()
                // Direct path-preserving routes to health endpoints
                .route("project-health-direct", r -> r
                        .path("/api/projects/health")
                        .uri("http://project-service:8082"))
                        
                .route("task-health-direct", r -> r
                        .path("/api/tasks/health")
                        .uri("http://task-service:8083"))
                        
                .route("integration-health-direct", r -> r
                        .path("/api/integrations/health")
                        .uri("http://integration-service:8084"))
                        
                .route("auth-health-direct", r -> r
                        .path("/api/auth/health")
                        .uri("http://auth-service:8081"))
                        
                .route("admin-health-direct", r -> r
                        .path("/api/admin/health")
                        .uri("http://admin-service:8085"))
                .build();
    }
}
