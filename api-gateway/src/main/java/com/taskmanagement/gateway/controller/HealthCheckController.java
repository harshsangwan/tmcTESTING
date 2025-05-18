package com.taskmanagement.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    private final WebClient.Builder webClientBuilder;
    
    public HealthCheckController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    @GetMapping("/gateway-health")
    public ResponseEntity<Map<String, String>> gatewayHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "API Gateway");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/check-auth-service")
    public Mono<ResponseEntity<String>> checkAuthService() {
        return webClientBuilder.build()
            .get()
            .uri("http://auth-service:8081/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> ResponseEntity.ok("Auth Service Status: " + response))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Auth Service Error: " + e.getMessage())));
    }
}
