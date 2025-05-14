package com.taskmanagement.task.repository;

import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    
    List<TaskComment> findByTaskOrderByCreatedAtDesc(Task task);
    
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<TaskComment> findByUserId(Long userId);
    
    void deleteByTaskId(Long taskId);
}