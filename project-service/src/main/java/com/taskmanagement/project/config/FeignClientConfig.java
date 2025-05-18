package com.taskmanagement.project.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader("Authorization");
                if (authHeader != null && !authHeader.isEmpty()) {
                    requestTemplate.header("Authorization", authHeader);
                }
                
                // Forward user context headers
                String userId = attrs.getRequest().getHeader("X-User-Id");
                String userEmail = attrs.getRequest().getHeader("X-User-Email");
                String userRole = attrs.getRequest().getHeader("X-User-Role");
                
                if (userId != null) requestTemplate.header("X-User-Id", userId);
                if (userEmail != null) requestTemplate.header("X-User-Email", userEmail);
                if (userRole != null) requestTemplate.header("X-User-Role", userRole);
            }
        };
    }
}
