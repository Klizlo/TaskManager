package com.example.TaskManager.exception;

public class UserNotExistsException extends RuntimeException{

    public UserNotExistsException(Long id){
        super("User id " + id + " does not exist");
    }

    public UserNotExistsException(String username) {
        super("User " + username + " does not exist");
    }
}
