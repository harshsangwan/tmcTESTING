package com.taskmanagement.task.repository;

import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.Task.TaskStatus;
import com.taskmanagement.task.model.entity.Task.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByProjectId(Long projectId);
    
    List<Task> findByAssignedTo(Long userId);
    
    List<Task> findByCreatedBy(Long userId);
    
    List<Task> findByCreatedByOrAssignedTo(Long createdBy, Long assignedTo);
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByPriority(TaskPriority priority);
    
    List<Task> findByDueDateBefore(LocalDate date);
    
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    List<Task> findByProjectIdAndAssignedTo(Long projectId, Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Task> searchTasksByProjectId(@Param("projectId") Long projectId, 
                                    @Param("searchTerm") String searchTerm);
    
    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Task> searchTasks(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
}