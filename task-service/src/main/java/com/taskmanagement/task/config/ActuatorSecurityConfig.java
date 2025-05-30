package com.taskmanagement.task.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(1)  // Higher priority than other security configurations
public class ActuatorSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .securityMatcher("/actuator/**")
            .securityMatcher("/health")
            .securityMatcher("/api/tasks/health")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/api/tasks/health").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
