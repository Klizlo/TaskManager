package com.example.TaskManager.exception;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String name) {
        super("Role " + name + " already exists");
    }
}
