#!/bin/bash
echo "=== Creating Minimal API Gateway Fix ==="

# Create a simplified application.properties for the API Gateway
cat > api-gateway/src/main/resources/application-docker.properties << 'EOF'
# Minimal API Gateway configuration
spring.application.name=api-gateway
server.port=8080

# Disable circuit breakers and load balancing
spring.cloud.circuitbreaker.enabled=false
spring.cloud.gateway.filter.circuit-breaker.enabled=false

# Disable Eureka for auth-service routing
spring.cloud.discovery.enabled=true
eureka.client.enabled=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka/

# Simple, direct HTTP route for auth-service
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://auth-service:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**

# Basic CORS configuration
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*

# Debug logging
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.reactor.netty.http.client=DEBUG
logging.level.org.springframework.web.server=DEBUG
EOF

# Create an application.yml file (sometimes Spring Boot prefers YAML)
cat > api-gateway/src/main/resources/application-docker.yml << 'EOF'
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
      - id: auth-service
        uri: http://auth-service:8081
        predicates:
        - Path=/api/auth/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
    circuitbreaker:
      enabled: false
    discovery:
      enabled: true
  main:
    web-application-type: reactive

server:
  port: 8080

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: DEBUG
    reactor.netty.http.client: DEBUG
EOF

# Create a simple route configuration class
cat > api-gateway/src/main/java/com/taskmanagement/gateway/config/SimpleRouteConfig.java << 'EOF'
package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class SimpleRouteConfig {
    
    @Bean
    @Primary
    public RouteLocator simpleRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://auth-service:8081"))
                .build();
    }
}
EOF

echo "Minimal configuration created. Now restart the API Gateway..."