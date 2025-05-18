#!/bin/bash
echo "=== Creating Raw Passthrough Fix ==="

# Create extremely simple passthrough configuration
cat > api-gateway/src/main/resources/application-docker.yml << 'EOF'
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: auth-service-passthrough
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**
      default-filters: []
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
    loadbalancer:
      ribbon:
        enabled: false
    circuit-breaker:
      enabled: false

server:
  port: 8080

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: DEBUG
    reactor.netty.http.client: TRACE
EOF

# Create a bare-bones route configuration without any filters
cat > api-gateway/src/main/java/com/taskmanagement/gateway/config/RawPassthroughConfig.java << 'EOF'
package com.taskmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class RawPassthroughConfig {
    
    @Bean
    @Primary
    public RouteLocator rawRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-passthrough", r -> r
                        .path("/api/auth/**")
                        .uri("http://auth-service:8081"))
                .build();
    }
}
EOF

# Disable all existing filters by renaming them
for f in api-gateway/src/main/java/com/taskmanagement/gateway/filter/*.java; do
    if [ -f "$f" ]; then
        mv "$f" "${f}.bak"
        echo "Disabled filter: $f"
    fi
done

echo "Raw passthrough configuration created."
echo "Now restart the API Gateway..."