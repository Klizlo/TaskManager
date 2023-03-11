package com.example.TaskManager.exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(Long id){
        super("User id " + id + " does not exist");
    }

    public UserNotFoundException(String username) {
        super("User " + username + " does not exist");
    }
}
