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
