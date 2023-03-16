package com.example.TaskManager.service;

import com.example.TaskManager.model.User;

import java.util.List;

public interface IUserService {

    List<User> findAllUsers();
    User findUserById(Long id);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    User addUser(User user);
    User updateUser(Long id, User user);
    User modifyUserRoles(Long userId, List<Long> roleIds);
    void deleteUser(Long id);
}
