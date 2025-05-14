package com.taskmanagement.integration.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationStatus status;
    
    @Column(name = "connection_url")
    private String connectionUrl;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "access_token")
    private String accessToken;
    
    @Column(name = "refresh_token")
    private String refreshToken;
    
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;
    
    @Column(name = "credentials", columnDefinition = "TEXT")
    private String credentials; // Encrypted JSON of additional credentials
    
    @Column(name = "settings", columnDefinition = "TEXT")
    private String settings; // JSON of settings
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum IntegrationType {
        CALENDAR,
        COMMUNICATION,
        VERSION_CONTROL,
        STORAGE,
        OTHER
    }
    
    public enum IntegrationStatus {
        CONNECTED,
        DISCONNECTED,
        PENDING,
        ERROR
    }
}