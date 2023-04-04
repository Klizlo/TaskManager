package com.example.TaskManager.dto;

import com.example.TaskManager.model.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskDtoMapper {

    public static List<TaskDto> mapToTaskDtos(List<Task> tasks) {
        return tasks.stream().map(TaskDtoMapper::mapToTaskDto).collect(Collectors.toList());
    }

    public static TaskDto mapToTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .category(task.getCategory() != null ?
                        CategoryDtoMapper.mapToCategoryDto(task.getCategory()) : null)
                .owner(UserDtoMapper.mapToUserDto(task.getOwner()))
                .build();
    }

}
