package com.example.TaskManager.service;

import com.example.TaskManager.model.Task;

import java.util.List;

public interface ITaskService {

    List<Task> findAllTasks();
    Task findTask(Long id);
    Task addTask(Task task);
    Task updateTask(Long id, Task task);
    void deleteTask(Long id);

}
