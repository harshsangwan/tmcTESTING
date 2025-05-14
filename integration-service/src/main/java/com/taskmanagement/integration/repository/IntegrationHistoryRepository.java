package com.taskmanagement.integration.repository;

import com.taskmanagement.integration.model.entity.Integration;
import com.taskmanagement.integration.model.entity.IntegrationHistory;
import com.taskmanagement.integration.model.entity.IntegrationHistory.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IntegrationHistoryRepository extends JpaRepository<IntegrationHistory, Long> {
    
    List<IntegrationHistory> findByIntegrationOrderByCreatedAtDesc(Integration integration);
    
    List<IntegrationHistory> findByIntegrationIdOrderByCreatedAtDesc(Long integrationId);
    
    List<IntegrationHistory> findByUserId(Long userId);
    
    List<IntegrationHistory> findByUserIdAndIntegrationId(Long userId, Long integrationId);
    
    List<IntegrationHistory> findByActionType(ActionType actionType);
    
    List<IntegrationHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<IntegrationHistory> findTop10ByIntegrationIdOrderByCreatedAtDesc(Long integrationId);
}