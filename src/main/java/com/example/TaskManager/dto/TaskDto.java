package com.example.TaskManager.dto;

import com.example.TaskManager.model.Task;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class TaskDto {

    private Long id;
    private String name;
    private String description;
    private Task.Priority priority;
    private LocalDateTime deadline;
    private UserDto owner;

}
