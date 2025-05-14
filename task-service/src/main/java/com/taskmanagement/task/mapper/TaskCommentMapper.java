package com.taskmanagement.task.mapper;

import com.taskmanagement.task.model.dto.TaskCommentDto;
import com.taskmanagement.task.model.entity.Task;
import com.taskmanagement.task.model.entity.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper {
    
    @Mapping(target = "taskId", source = "task.id")
    TaskCommentDto toDto(TaskComment comment);
    
    @Mapping(target = "task", source = "taskId", qualifiedByName = "mapTaskId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskComment toEntity(TaskCommentDto commentDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TaskCommentDto commentDto, @MappingTarget TaskComment comment);
    
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