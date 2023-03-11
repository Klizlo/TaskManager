package com.example.TaskManager.service;

import com.example.TaskManager.model.Role;

import java.util.List;

public interface IRoleService {

    List<Role> findAllRoles();
    List<Role> findAllRolesById(List<Long> ids);
    Role findRoleById(Long id);
    Role findRoleByName(String name);
    Role addRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);

}
