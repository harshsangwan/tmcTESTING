package com.taskmanagement.task.mapper;

import com.taskmanagement.task.model.dto.TaskHistoryDto;
import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TaskHistoryMapper {
    
    @Mapping(target = "taskId", source = "task.id")
    TaskHistoryDto toDto(TaskHistory history);
    
    @Mapping(target = "task", source = "taskId", qualifiedByName = "mapTaskId")
    @Mapping(target = "createdAt", ignore = true)
    TaskHistory toEntity(TaskHistoryDto historyDto);
    
    @Named("mapTaskId")
    default Task mapTaskId(Long taskId) {
        if (taskId == null) {
            return null;
        }
        Task task = new Task();
        task.setId(taskId);
        return task;
    }
}