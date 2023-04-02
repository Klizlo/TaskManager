package com.example.TaskManager.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String name) {
        super("Category already exists with: " + name);
    }
}
