package com.example.TaskManager.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Category id " + id + " not found");
    }
}
