package com.taskmanagement.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.gateway.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Paths that are excluded from authentication
    private final Set<String> excludedPaths = Set.of(
        "/api/auth/login", 
        "/api/auth/register", 
        "/api/auth/refresh-token",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
    );

    public AuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            // Skip authentication for OPTIONS requests (CORS preflight)
            if (request.getMethod().matches("OPTIONS")) {
                return chain.filter(exchange);
            }
            
            // Skip authentication for excluded paths
            if (isPathExcluded(path)) {
                return chain.filter(exchange);
            }

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
            }

            // Extract the token from the Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate the token
                if (jwtUtil.isTokenExpired(token)) {
                    return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
                }
                
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Add user information to headers for downstream services
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                ServerHttpRequest modifiedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .build();

                log.debug("User {} with ID {} and role {} is accessing {}", 
                        username, userId, role, request.getPath());
                
                // Check role-based access if configured
                if (config.isRoleBasedAccessControl() && !hasAccess(path, role, config)) {
                    return onError(exchange, "Access denied - Insufficient privileges", HttpStatus.FORBIDDEN);
                }

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (ExpiredJwtException e) {
                log.error("JWT token expired", e);
                return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
            } catch (SignatureException e) {
                log.error("Invalid JWT signature", e);
                return onError(exchange, "Invalid JWT signature", HttpStatus.UNAUTHORIZED);
            } catch (MalformedJwtException e) {
                log.error("Invalid JWT token", e);
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token", e);
                return onError(exchange, "Unsupported JWT token", HttpStatus.UNAUTHORIZED);
            } catch (IllegalArgumentException e) {
                log.error("JWT claims string is empty", e);
                return onError(exchange, "JWT claims string is empty", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("Authentication error", e);
                return onError(exchange, "Authentication error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
    
    private boolean isPathExcluded(String path) {
        return excludedPaths.stream().anyMatch(path::endsWith);
    }
    
    private boolean hasAccess(String path, String role, Config config) {
        // Admin has access to everything
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        
        // Check path-specific roles
        if (path.startsWith("/api/admin/") && !("ADMIN".equalsIgnoreCase(role))) {
            return false;
        }
        
        // Project manager has access to projects and tasks
        if ("PROJECT_MANAGER".equalsIgnoreCase(role)) {
            return !path.startsWith("/api/admin/");
        }
        
        // Regular users have restricted access
        if ("USER".equalsIgnoreCase(role)) {
            return !path.startsWith("/api/admin/") && 
                   !path.contains("/projects/delete") && 
                   !path.contains("/tasks/delete-all");
        }
        
        // By default, deny if role is unknown
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        log.error("Authentication error: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("error", httpStatus.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", exchange.getRequest().getPath().value());
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // Configuration properties for the filter
        private boolean roleBasedAccessControl = true;
        private List<String> adminPaths = List.of("/api/admin/**");
        private List<String> publicPaths = List.of("/api/auth/login", "/api/auth/register");

        public boolean isRoleBasedAccessControl() {
            return roleBasedAccessControl;
        }

        public void setRoleBasedAccessControl(boolean roleBasedAccessControl) {
            this.roleBasedAccessControl = roleBasedAccessControl;
        }

        public List<String> getAdminPaths() {
            return adminPaths;
        }

        public void setAdminPaths(List<String> adminPaths) {
            this.adminPaths = adminPaths;
        }

        public List<String> getPublicPaths() {
            return publicPaths;
        }

        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }
    }
}