package com.taskmanagement.gateway.config;

import com.taskmanagement.gateway.filter.AuthenticationFilter;
import com.taskmanagement.gateway.filter.RateLimiterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RouteConfig {

    private final AuthenticationFilter authenticationFilter;
    private final RateLimiterFilter rateLimiterFilter;

    public RouteConfig(AuthenticationFilter authenticationFilter, 
                       RateLimiterFilter rateLimiterFilter) {
        this.authenticationFilter = authenticationFilter;
        this.rateLimiterFilter = rateLimiterFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring API Gateway routes");
        
        // Configure rate limiter for all routes
        RateLimiterFilter.Config rateLimiterConfig = new RateLimiterFilter.Config();
        rateLimiterConfig.setLimitForPeriod(100);
        
        return builder.routes()
                // Auth Service Routes - No authentication filter, public endpoints
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("auth-service")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))
                
                // Health check route for auth service
                .route("auth-health", r -> r
                        .path("/api/auth/health")
                        .filters(f -> f
                                .rewritePath("/api/auth/health", "/actuator/health"))
                        .uri("lb://auth-service"))
                
                // Project Service Routes - With authentication, rate limiting, circuit breaker
                .route("project-service", r -> r
                        .path("/api/projects/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/projects/(?<segment>.*)", "/api/projects/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("project-service")
                                        .setFallbackUri("forward:/fallback/projects")))
                        .uri("lb://project-service"))
                
                // Health check route for project service
                .route("project-health", r -> r
                        .path("/api/projects/health")
                        .filters(f -> f
                                .rewritePath("/api/projects/health", "/actuator/health"))
                        .uri("lb://project-service"))
                
                // Task Service Routes - With authentication, rate limiting, circuit breaker
                .route("task-service", r -> r
                        .path("/api/tasks/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/tasks/(?<segment>.*)", "/api/tasks/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("task-service")
                                        .setFallbackUri("forward:/fallback/tasks")))
                        .uri("lb://task-service"))
                
                // Health check route for task service
                .route("task-health", r -> r
                        .path("/api/tasks/health")
                        .filters(f -> f
                                .rewritePath("/api/tasks/health", "/actuator/health"))
                        .uri("lb://task-service"))
                
                // Admin Service Routes - With authentication, rate limiting, circuit breaker
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/admin/(?<segment>.*)", "/api/admin/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("admin-service")
                                        .setFallbackUri("forward:/fallback/admin")))
                        .uri("lb://admin-service"))
                
                // Health check route for admin service
                .route("admin-health", r -> r
                        .path("/api/admin/health")
                        .filters(f -> f
                                .rewritePath("/api/admin/health", "/actuator/health"))
                        .uri("lb://admin-service"))
                
                // Integration Service Routes - With authentication, rate limiting, circuit breaker
                .route("integration-service", r -> r
                        .path("/api/integrations/**")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/integrations/(?<segment>.*)", "/api/integrations/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("integration-service")
                                        .setFallbackUri("forward:/fallback/integrations")))
                        .uri("lb://integration-service"))
                
                // Health check route for integration service
                .route("integration-health", r -> r
                        .path("/api/integrations/health")
                        .filters(f -> f
                                .rewritePath("/api/integrations/health", "/actuator/health"))
                        .uri("lb://integration-service"))
                
                .build();
    }
}