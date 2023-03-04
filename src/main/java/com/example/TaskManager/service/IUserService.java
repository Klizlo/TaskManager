package com.example.TaskManager.service;

import com.example.TaskManager.model.User;

import java.util.List;

public interface IUserService {

    List<User> findAllUsers();
    User findUserById(Long id);
    User findUserByEmail(String email);
    User addUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);

}
