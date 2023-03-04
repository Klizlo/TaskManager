package com.example.TaskManager.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User " + email + " already exists");
    }
}
