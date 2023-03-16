package com.example.TaskManager.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String detail) {
        super("User already exists with: " + detail);
    }
}
