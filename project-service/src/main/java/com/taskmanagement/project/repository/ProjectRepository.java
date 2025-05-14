package com.taskmanagement.project.repository;

import com.taskmanagement.project.model.entity.Project;
import com.taskmanagement.project.model.entity.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedBy(Long userId);
    
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.userId = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);
    
    List<Project> findByStatus(ProjectStatus status);
    
    List<Project> findByEndDateBefore(LocalDate date);
    
    List<Project> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdBy = :userId")
    Long countByCreatedBy(@Param("userId") Long userId);
}