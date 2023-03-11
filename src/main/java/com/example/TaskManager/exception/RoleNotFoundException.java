package com.example.TaskManager.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(Long id){
        super("Role id " + id + " does not exist");
    }

    public RoleNotFoundException(String name){
        super("Role id " + name + " does not exist");
    }

}
