package com.taskmanagement.admin.repository;

import com.taskmanagement.admin.model.entity.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    
    Page<LogEntry> findByOrderByTimestampDesc(Pageable pageable);
    
    Page<LogEntry> findByLevelOrderByTimestampDesc(String level, Pageable pageable);
    
    Page<LogEntry> findBySourceOrderByTimestampDesc(String source, Pageable pageable);
    
    Page<LogEntry> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    Page<LogEntry> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT DISTINCT l.source FROM LogEntry l")
    List<String> findAllSources();
    
    @Query("SELECT l FROM LogEntry l WHERE " +
           "(:level IS NULL OR l.level = :level) AND " +
           "(:source IS NULL OR l.source = :source) AND " +
           "(:userId IS NULL OR l.userId = :userId) AND " +
           "(:startDate IS NULL OR l.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR l.timestamp <= :endDate) AND " +
           "(:search IS NULL OR LOWER(l.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY l.timestamp DESC")
    Page<LogEntry> findWithFilters(
            @Param("level") String level,
            @Param("source") String source,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("search") String search,
            Pageable pageable);
}