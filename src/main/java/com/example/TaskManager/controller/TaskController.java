package com.example.TaskManager.controller;

import com.example.TaskManager.dto.TaskDto;
import com.example.TaskManager.dto.TaskDtoMapper;
import com.example.TaskManager.exception.ForbiddenException;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.service.ITaskService;
import com.example.TaskManager.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    private final ITaskService taskService;
    private final IUserService userService;

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<TaskDto> findAllTasks() {
        return TaskDtoMapper.mapToTaskDtos(taskService.findAllTasks());
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public TaskDto findTask(@PathVariable("id") Long id) {

        Task task = taskService.findTask(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if (!task.getOwner().getId().equals(loggedUser.getId()) &&
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        return TaskDtoMapper.mapToTaskDto(task);
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto addTask(@Valid @RequestBody Task task) {
        return TaskDtoMapper.mapToTaskDto(taskService.addTask(task));
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public TaskDto updateTask(@PathVariable("id") Long id, @Valid @RequestBody Task task) {
        return TaskDtoMapper.mapToTaskDto(taskService.updateTask(id, task));
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
    }

}
