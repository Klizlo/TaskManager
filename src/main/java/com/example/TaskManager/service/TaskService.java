package com.example.TaskManager.service;

import com.example.TaskManager.exception.ForbiddenException;
import com.example.TaskManager.exception.TaskNotFoundException;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService{

    private final TaskRepository taskRepository;
    private final IUserService userService;

    @Override
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task findTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    @Transactional
    public Task addTask(Task task) {

        User owner = userService.findUserById(task.getOwner().getId());

        task.setOwner(owner);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, Task task) {

        Task taskToEdit = findTask(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(!taskToEdit.getOwner().getId().equals(loggedUser.getId())
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        if(task.getName() != null && !task.getName().equals(taskToEdit.getName()))
            taskToEdit.setName(task.getName());

        if(task.getPriority() != null && !task.getPriority().equals(taskToEdit.getPriority()))
            taskToEdit.setPriority(task.getPriority());

        taskToEdit.setDescription(task.getDescription());
        taskToEdit.setDeadline(task.getDeadline());

        return taskRepository.save(taskToEdit);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {

        if(!taskRepository.existsById(id))
            throw new TaskNotFoundException(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(loggedUser.getTasks().stream().map(Task::getId).noneMatch(userTask -> userTask.equals(id))
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        taskRepository.deleteById(id);
    }
}
