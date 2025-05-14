package com.taskmanagement.gateway.config;

import com.taskmanagement.gateway.filter.AuthenticationFilter;
import com.taskmanagement.gateway.filter.RateLimiterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;

@Configuration
@Slf4j
public class RouteConfig {

    private final AuthenticationFilter authenticationFilter;
    private final RateLimiterFilter rateLimiterFilter;
    private final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    public RouteConfig(AuthenticationFilter authenticationFilter, 
                       RateLimiterFilter rateLimiterFilter,
                       ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
        this.authenticationFilter = authenticationFilter;
        this.rateLimiterFilter = rateLimiterFilter;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // Configure rate limiter for all routes
        RateLimiterFilter.Config rateLimiterConfig = new RateLimiterFilter.Config();
        rateLimiterConfig.setLimitForPeriod(100);
        
        return builder.routes()
                // Auth Service Routes - No authentication filter but with rate limiting
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(rateLimiterFilter.apply(rateLimiterConfig))
                                .rewritePath("/api/auth/(?<segment>.*)", "/api/auth/${segment}")
                                .circuitBreaker(config -> config
                                        .setName("auth-service")
                                        .setFallbackUri("/fallback/auth")))
                        // .uri("http://auth-service:8081"))
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
                                        .setFallbackUri("/fallback/projects")))
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
                                        .setFallbackUri("/fallback/tasks")))
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
                                        .setFallbackUri("/fallback/admin")))
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
                                        .setFallbackUri("/fallback/integrations")))
                        .uri("lb://integration-service"))
                
                // Health check endpoint - No authentication required
                .route("health-check", r -> r
                        .path("/health/**")
                        .filters(f -> f
                                .filter(rateLimiterFilter.apply(rateLimiterConfig)))
                        .uri("lb://api-gateway"))
                
                .build();
    }
}