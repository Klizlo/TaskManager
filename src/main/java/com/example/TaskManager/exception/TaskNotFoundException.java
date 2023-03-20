package com.example.TaskManager.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Task id " + id + " not found");
    }
}
