package com.taskmanagement.task.mapper;

import com.taskmanagement.task.model.dto.TaskDto;
import com.taskmanagement.task.model.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    
    @Mapping(target = "comments", ignore = true)
    TaskDto toDto(Task task);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Task toEntity(TaskDto taskDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void updateEntity(TaskDto taskDto, @MappingTarget Task task);
}