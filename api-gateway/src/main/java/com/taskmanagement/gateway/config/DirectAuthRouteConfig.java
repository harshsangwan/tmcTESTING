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
