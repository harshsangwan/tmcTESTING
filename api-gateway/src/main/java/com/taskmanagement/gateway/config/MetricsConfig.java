package com.taskmanagement.gateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    
    // Track active requests
    private final AtomicInteger activeRequests;
    
    // Counters for requests by service
    private final Counter authServiceRequests;
    private final Counter projectServiceRequests;
    private final Counter taskServiceRequests;
    private final Counter adminServiceRequests;
    private final Counter integrationServiceRequests;
    
    // Counters for errors
    private final Counter clientErrors;
    private final Counter serverErrors;
    
    // Timers for response time
    private final Timer authServiceTimer;
    private final Timer projectServiceTimer;
    private final Timer taskServiceTimer;
    private final Timer adminServiceTimer;
    private final Timer integrationServiceTimer;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.activeRequests = meterRegistry.gauge("gateway.requests.active", new AtomicInteger(0));
        
        // Initialize counters
        this.authServiceRequests = Counter.builder("gateway.requests.service")
                .tag("service", "auth-service")
                .description("Number of requests to Auth Service")
                .register(meterRegistry);
                
        this.projectServiceRequests = Counter.builder("gateway.requests.service")
                .tag("service", "project-service")
                .description("Number of requests to Project Service")
                .register(meterRegistry);
                
        this.taskServiceRequests = Counter.builder("gateway.requests.service")
                .tag("service", "task-service")
                .description("Number of requests to Task Service")
                .register(meterRegistry);
                
        this.adminServiceRequests = Counter.builder("gateway.requests.service")
                .tag("service", "admin-service")
                .description("Number of requests to Admin Service")
                .register(meterRegistry);
                
        this.integrationServiceRequests = Counter.builder("gateway.requests.service")
                .tag("service", "integration-service")
                .description("Number of requests to Integration Service")
                .register(meterRegistry);
        
        // Initialize error counters
        this.clientErrors = Counter.builder("gateway.errors")
                .tag("type", "client")
                .description("Number of client errors (4xx)")
                .register(meterRegistry);
                
        this.serverErrors = Counter.builder("gateway.errors")
                .tag("type", "server")
                .description("Number of server errors (5xx)")
                .register(meterRegistry);
        
        // Initialize timers
        this.authServiceTimer = Timer.builder("gateway.response.time")
                .tag("service", "auth-service")
                .description("Response time for Auth Service")
                .register(meterRegistry);
                
        this.projectServiceTimer = Timer.builder("gateway.response.time")
                .tag("service", "project-service")
                .description("Response time for Project Service")
                .register(meterRegistry);
                
        this.taskServiceTimer = Timer.builder("gateway.response.time")
                .tag("service", "task-service")
                .description("Response time for Task Service")
                .register(meterRegistry);
                
        this.adminServiceTimer = Timer.builder("gateway.response.time")
                .tag("service", "admin-service")
                .description("Response time for Admin Service")
                .register(meterRegistry);
                
        this.integrationServiceTimer = Timer.builder("gateway.response.time")
                .tag("service", "integration-service")
                .description("Response time for Integration Service")
                .register(meterRegistry);
    }
    
    @Bean
    public WebFilter metricsFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            // Increment active requests
            activeRequests.incrementAndGet();
            
            // Record request by service
            if (path.startsWith("/api/auth/")) {
                authServiceRequests.increment();
            } else if (path.startsWith("/api/projects/")) {
                projectServiceRequests.increment();
            } else if (path.startsWith("/api/tasks/")) {
                taskServiceRequests.increment();
            } else if (path.startsWith("/api/admin/")) {
                adminServiceRequests.increment();
            } else if (path.startsWith("/api/integrations/")) {
                integrationServiceRequests.increment();
            }
            
            // Start timer
            long start = System.nanoTime();
            
            return chain.filter(exchange).doFinally(signalType -> {
                // Decrement active requests
                activeRequests.decrementAndGet();
                
                // Calculate response time
                long responseTime = System.nanoTime() - start;
                
                // Record timer based on service
                if (path.startsWith("/api/auth/")) {
                    authServiceTimer.record(responseTime, TimeUnit.NANOSECONDS);
                } else if (path.startsWith("/api/projects/")) {
                    projectServiceTimer.record(responseTime, TimeUnit.NANOSECONDS);
                } else if (path.startsWith("/api/tasks/")) {
                    taskServiceTimer.record(responseTime, TimeUnit.NANOSECONDS);
                } else if (path.startsWith("/api/admin/")) {
                    adminServiceTimer.record(responseTime, TimeUnit.NANOSECONDS);
                } else if (path.startsWith("/api/integrations/")) {
                    integrationServiceTimer.record(responseTime, TimeUnit.NANOSECONDS);
                }
                
                // Record errors
                int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() 
                    : 0;
                    
                if (statusCode >= 400 && statusCode < 500) {
                    clientErrors.increment();
                } else if (statusCode >= 500) {
                    serverErrors.increment();
                }
            });
        };
    }
}