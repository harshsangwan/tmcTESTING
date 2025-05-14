package com.taskmanagement.integration.repository;

import com.taskmanagement.integration.model.entity.Integration;
import com.taskmanagement.integration.model.entity.Integration.IntegrationType;
import com.taskmanagement.integration.model.entity.Integration.IntegrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Long> {
    
    List<Integration> findByUserId(Long userId);
    
    List<Integration> findByType(IntegrationType type);
    
    List<Integration> findByStatus(IntegrationStatus status);
    
    List<Integration> findByUserIdAndType(Long userId, IntegrationType type);
    
    List<Integration> findByUserIdAndStatus(Long userId, IntegrationStatus status);
    
    List<Integration> findByUserIdAndTypeAndStatus(Long userId, IntegrationType type, IntegrationStatus status);
}