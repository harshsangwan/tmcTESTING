package com.taskmanagement.integration.controller;

import com.taskmanagement.integration.model.dto.IntegrationConnectRequest;
import com.taskmanagement.integration.model.dto.IntegrationDto;
import com.taskmanagement.integration.model.entity.IntegrationHistory;
import com.taskmanagement.integration.security.UserPrincipal;
import com.taskmanagement.integration.service.IntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping
    public ResponseEntity<List<IntegrationDto>> getUserIntegrations(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to get all integrations for user: {}", currentUser.getId());
        return ResponseEntity.ok(integrationService.getUserIntegrations(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntegrationDto> getIntegrationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to get integration with id: {}", id);
        return ResponseEntity.ok(integrationService.getIntegrationById(id, currentUser));
    }

    @PostMapping("/connect")
    public ResponseEntity<IntegrationDto> connectIntegration(
            @Valid @RequestBody IntegrationConnectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to connect integration with id: {}", request.getIntegrationId());
        return ResponseEntity.ok(integrationService.connectIntegration(request, currentUser));
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<IntegrationDto> disconnectIntegration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to disconnect integration with id: {}", id);
        return ResponseEntity.ok(integrationService.disconnectIntegration(id, currentUser));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<IntegrationDto> syncIntegration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to sync integration with id: {}", id);
        return ResponseEntity.ok(integrationService.syncIntegration(id, currentUser));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<IntegrationHistory>> getIntegrationHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Request to get history for integration with id: {}", id);
        return ResponseEntity.ok(integrationService.getIntegrationHistory(id, currentUser));
    }
    
    @GetMapping("/oauth/callback/{provider}")
    public ResponseEntity<Map<String, String>> handleOAuthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error) {
        log.info("OAuth callback received for provider: {}", provider);
        
        if (error != null) {
            log.error("OAuth error: {}", error);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", error
            ));
        }
        
        // This endpoint just returns the code and state for the frontend to use
        // The actual connection will be made by the frontend calling the connect endpoint
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "provider", provider,
            "code", code,
            "state", state
        ));
    }
}