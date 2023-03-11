package com.example.TaskManager.service;

import com.example.TaskManager.exception.RoleAlreadyExistsException;
import com.example.TaskManager.exception.RoleNotFoundException;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{

    private final RoleRepository roleRepository;

    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<Role> findAllRolesById(List<Long> ids) {
        return roleRepository.findAllById(ids);
    }

    @Override
    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
    }

    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findRoleByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }

    @Override
    @Transactional
    public Role addRole(Role role) {
        if(roleRepository.existsByName(role.getName()))
            throw new RoleAlreadyExistsException(role.getName());

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateRole(Long id, Role role) {

        Role foundRole = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        if (roleRepository.existsByName(role.getName()))
            throw new RoleAlreadyExistsException(role.getName());

        foundRole.setName(role.getName());

        return roleRepository.save(foundRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id))
            throw new RoleNotFoundException(id);

        roleRepository.deleteById(id);
    }
}
