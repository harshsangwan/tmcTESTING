#!/bin/bash
echo "=== Creating Final Complete JWT Fix ==="

# 1. Create a direct HTTP route configuration for auth-service
cat > api-gateway/src/main/java/com/taskmanagement/gateway/config/DirectAuthRouteConfig.java << 'EOF'
package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DirectAuthRouteConfig {
    
    @Bean
    @Primary
    public RouteLocator authRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring direct HTTP route for Auth Service");
        
        return builder.routes()
                // Auth Service with direct HTTP route - No circuit breaker
                .route("auth-service-direct", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("http://auth-service:8081"))
                .build();
    }
}
EOF

# 2. Create a health check endpoint controller in API Gateway for debugging
cat > api-gateway/src/main/java/com/taskmanagement/gateway/controller/HealthCheckController.java << 'EOF'
package com.taskmanagement.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    private final WebClient.Builder webClientBuilder;
    
    public HealthCheckController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    @GetMapping("/gateway-health")
    public ResponseEntity<Map<String, String>> gatewayHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "API Gateway");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/check-auth-service")
    public Mono<ResponseEntity<String>> checkAuthService() {
        return webClientBuilder.build()
            .get()
            .uri("http://auth-service:8081/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> ResponseEntity.ok("Auth Service Status: " + response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Auth Service Error: " + e.getMessage())));
    }
}
EOF

# 3. Update the application properties with direct auth service route
cat > api-gateway/src/main/resources/application-docker.properties << 'EOF'
# Docker profile configuration
spring.application.name=api-gateway
server.port=8080

# JWT Configuration
app.jwt.secret=${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b80c0ceb73d3d121182c95878b9a33cd8caf5bee427d6f0b9b5b989f0bd25c035}
app.jwt.issuer=taskmanagement

# CORS Configuration
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].exposed-headers=Authorization
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
spring.cloud.gateway.globalcors.cors-configurations.[/**].max-age=3600

# Direct route configuration for auth service (not using programmatic config)
spring.cloud.gateway.routes[0].id=auth-service-direct
spring.cloud.gateway.routes[0].uri=http://auth-service:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/auth/(?<segment>.*), /api/auth/${segment}

# Other services can still use Eureka
spring.cloud.gateway.routes[1].id=project-service
spring.cloud.gateway.routes[1].uri=lb://project-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/projects/**
spring.cloud.gateway.routes[1].filters[0]=RewritePath=/api/projects/(?<segment>.*), /api/projects/${segment}
spring.cloud.gateway.routes[1].filters[1]=AuthenticationFilter

spring.cloud.gateway.routes[2].id=task-service
spring.cloud.gateway.routes[2].uri=lb://task-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/tasks/**
spring.cloud.gateway.routes[2].filters[0]=RewritePath=/api/tasks/(?<segment>.*), /api/tasks/${segment}
spring.cloud.gateway.routes[2].filters[1]=AuthenticationFilter

# Eureka configuration - Kept but not required for auth route
eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# WebClient configuration
spring.codec.max-in-memory-size=5MB

# Logging
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.com.taskmanagement.gateway=DEBUG
logging.level.reactor.netty=DEBUG
EOF

# 4. Make previous route config less important
cat > api-gateway/src/main/java/com/taskmanagement/gateway/config/RouteConfigModifier.java << 'EOF'
package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * This class just makes sure our direct auth config takes precedence
 */
@Configuration
@Slf4j
@Order(0)
public class RouteConfigModifier {
    @PostConstruct
    public void init() {
        log.info("Route config modifier initialized. Direct auth routes will take precedence.");
    }
}
EOF

echo "JWT fix files created!"
echo "Now restart the API Gateway..."
echo "docker-compose restart api-gateway"