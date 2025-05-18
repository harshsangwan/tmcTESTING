package com.taskmanagement.gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {

    // Store rate limiters by IP and by JWT token (if available)
    private final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    public RateLimiterFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = getClientIp(exchange);
            
            // Check if request has JWT token and is authenticated
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");
            
            // Get or create rate limiter based on authentication status
            if (isAuthenticated && config.isUserBasedRateLimiting()) {
                // Use JWT token based rate limiting for authenticated users (more permissive)
                String token = authHeader.substring(7);
                String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                
                if (userId != null) {
                    // Rate limit by user ID when available
                    RateLimiter rateLimiter = getUserRateLimiter(userId, config);
                    if (!checkRateLimit(rateLimiter, exchange, "User ID: " + userId)) {
                        return exchange.getResponse().setComplete();
                    }
                } else {
                    // Fallback to token-based rate limiting
                    RateLimiter rateLimiter = getUserRateLimiter(token, config);
                    if (!checkRateLimit(rateLimiter, exchange, "JWT token")) {
                        return exchange.getResponse().setComplete();
                    }
                }
            } else {
                // Use IP-based rate limiting for unauthenticated users (more restrictive)
                RateLimiter rateLimiter = getIpRateLimiter(clientIp, config);
                if (!checkRateLimit(rateLimiter, exchange, "IP: " + clientIp)) {
                    return exchange.getResponse().setComplete();
                }
            }

            // Rate limit not exceeded, continue with the request
            return chain.filter(exchange);
        };
    }
    
    private boolean checkRateLimit(RateLimiter rateLimiter, ServerWebExchange exchange, String identifier) {
        boolean permission = rateLimiter.acquirePermission();
        if (!permission) {
            log.warn("Rate limit exceeded for {}", identifier);
            
            // Add rate limit headers
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.add("X-RateLimit-Limit", String.valueOf(rateLimiter.getRateLimiterConfig().getLimitForPeriod()));
            headers.add("X-RateLimit-Remaining", "0");
            headers.add("Retry-After", "1"); // Suggest retry after 1 second
            
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }
        return true;
    }

    private RateLimiter getIpRateLimiter(String clientIp, Config config) {
        return ipRateLimiters.computeIfAbsent(clientIp, ip -> {
            RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(config.getLimitRefreshPeriodInSeconds()))
                    .limitForPeriod(config.getIpLimitForPeriod())
                    .timeoutDuration(Duration.ofMillis(config.getTimeoutDurationInMillis()))
                    .build();
            return RateLimiter.of("ip-rate-limiter-" + ip, rateLimiterConfig);
        });
    }
    
    private RateLimiter getUserRateLimiter(String userIdentifier, Config config) {
        return userRateLimiters.computeIfAbsent(userIdentifier, id -> {
            RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(config.getLimitRefreshPeriodInSeconds()))
                    .limitForPeriod(config.getUserLimitForPeriod())
                    .timeoutDuration(Duration.ofMillis(config.getTimeoutDurationInMillis()))
                    .build();
            return RateLimiter.of("user-rate-limiter-" + id, rateLimiterConfig);
        });
    }

    private String getClientIp(ServerWebExchange exchange) {
        // Try to get the client IP from X-Forwarded-For header
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs - the first one is the client IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Try other common headers
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to the remote address
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }

    public static class Config {
        private int ipLimitForPeriod = 50;     // More restrictive for unauthenticated users
        private int userLimitForPeriod = 100;  // More permissive for authenticated users
        private int limitRefreshPeriodInSeconds = 1;
        private int timeoutDurationInMillis = 0;
        private boolean userBasedRateLimiting = true;

        public int getIpLimitForPeriod() {
            return ipLimitForPeriod;
        }

        public void setIpLimitForPeriod(int ipLimitForPeriod) {
            this.ipLimitForPeriod = ipLimitForPeriod;
        }

        public int getUserLimitForPeriod() {
            return userLimitForPeriod;
        }

        public void setUserLimitForPeriod(int userLimitForPeriod) {
            this.userLimitForPeriod = userLimitForPeriod;
        }

        public int getLimitForPeriod() {
            return userLimitForPeriod; // For backward compatibility
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.userLimitForPeriod = limitForPeriod;
        }

        public int getLimitRefreshPeriodInSeconds() {
            return limitRefreshPeriodInSeconds;
        }

        public void setLimitRefreshPeriodInSeconds(int limitRefreshPeriodInSeconds) {
            this.limitRefreshPeriodInSeconds = limitRefreshPeriodInSeconds;
        }

        public int getTimeoutDurationInMillis() {
            return timeoutDurationInMillis;
        }

        public void setTimeoutDurationInMillis(int timeoutDurationInMillis) {
            this.timeoutDurationInMillis = timeoutDurationInMillis;
        }

        public boolean isUserBasedRateLimiting() {
            return userBasedRateLimiting;
        }

        public void setUserBasedRateLimiting(boolean userBasedRateLimiting) {
            this.userBasedRateLimiting = userBasedRateLimiting;
        }
    }
}