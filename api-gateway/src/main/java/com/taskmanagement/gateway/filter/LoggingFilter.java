package com.taskmanagement.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate a unique request ID for tracing
        String requestId = UUID.randomUUID().toString();
        
        // Log the incoming request
        log.info("Request: [{}] {} {} from {}",
                requestId,
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress().getAddress().getHostAddress());
        
        // Log request headers at debug level
        if (log.isDebugEnabled()) {
            request.getHeaders().forEach((name, values) -> {
                values.forEach(value -> {
                    // Don't log sensitive headers like Authorization fully
                    if (name.equalsIgnoreCase("Authorization")) {
                        log.debug("Request Header: [{}] {}: {}", requestId, name, 
                                value.length() > 15 ? value.substring(0, 15) + "..." : value);
                    } else {
                        log.debug("Request Header: [{}] {}: {}", requestId, name, value);
                    }
                });
            });
        }
        
        // Add the request ID to the request headers for tracing across services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        // Create a new exchange with the modified request
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        // Start timer for request duration
        long startTime = System.currentTimeMillis();
        
        // Continue the filter chain with logging of the response
        return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Response: [{}] {} {} - {} in {} ms",
                            requestId,
                            request.getMethod(),
                            request.getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration);
                }));
    }

    @Override
    public int getOrder() {
        // Ensure this runs before the other filters
        return -1;
    }
}