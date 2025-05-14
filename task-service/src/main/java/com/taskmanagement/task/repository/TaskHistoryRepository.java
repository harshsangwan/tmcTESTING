package com.taskmanagement.task.repository;

import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    
    List<TaskHistory> findByTaskOrderByCreatedAtDesc(Task task);
    
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<TaskHistory> findByUserId(Long userId);
    
    void deleteByTaskId(Long taskId);
}