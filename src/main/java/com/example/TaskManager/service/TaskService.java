package com.example.TaskManager.service;

import com.example.TaskManager.exception.TaskNotFoundException;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
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
    public Task addTask(Long userId, Task task) {

        User owner = userService.findUserById(userId);

        task.setOwner(owner);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, Task task) {

        Task taskToEdit = findTask(id);

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

        taskRepository.deleteById(id);
    }
}
