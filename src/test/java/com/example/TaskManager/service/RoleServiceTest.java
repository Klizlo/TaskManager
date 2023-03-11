package com.example.TaskManager.service;

import com.example.TaskManager.exception.RoleAlreadyExistsException;
import com.example.TaskManager.exception.RoleNotFoundException;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleService roleService;

    @Test
    void whenFindAllRoles_returnEmptyList() {

        List<Role> roles = roleService.findAllRoles();

        assertTrue(roles.isEmpty());
    }

    @Test
    void whenFindAllRoles_returnNonEmptyList() {

        Role role = new Role();
        role.setId(getRandomLong());
        role.setName("USER");

        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<Role> roles = roleService.findAllRoles();

        assertFalse(roles.isEmpty());
        assertEquals(role.getId(), roles.get(0).getId());
        assertEquals(role.getName(), roles.get(0).getName());
    }

    @Test
    void givenRoleIds_whenFindAllRolesByIds_returnEmptyList() {

        List<Role> roles = roleService.findAllRolesById(List.of(getRandomLong(), 10+getRandomLong()));

        assertTrue(roles.isEmpty());
    }

    @Test
    void givenEmptyRoleIds_whenFindAllRolesByIds_returnEmptyList() {

        List<Role> roles = roleService.findAllRolesById(List.of());

        assertTrue(roles.isEmpty());
    }

    @Test
    void givenRoleIds_whenFindAllRolesByIds_returnNonEmptyList() {

        Role roleUser = new Role();
        roleUser.setId(getRandomLong());
        roleUser.setName("USER");

        Role roleAdmin = new Role();
        roleAdmin.setId(10+getRandomLong());
        roleAdmin.setName("ADMIN");

        when(roleRepository.findAllById(any())).thenReturn(List.of(roleUser, roleAdmin));

        List<Role> roles = roleService.findAllRolesById(
                List.of(roleUser.getId(), roleAdmin.getId()));

        assertFalse(roles.isEmpty());
        assertTrue(roles.contains(roleUser));
        assertTrue(roles.contains(roleAdmin));
    }

    @Test
    void givenId_whenFindRoleById_returnRole() {

        Long id = getRandomLong();

        Role role = new Role();
        role.setId(id);
        role.setName("USER");

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        Role foundRole = roleService.findRoleById(id);

        assertEquals(role.getId(), foundRole.getId());
        assertEquals(role.getName(), foundRole.getName());
    }

    @Test
    void givenId_whenFindRoleById_throwException() {

        when(roleRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class,
                () -> roleService.findRoleById(getRandomLong()));
    }

    @Test
    void givenName_whenFindRoleByName_returnRole() {
        Long id = getRandomLong();

        Role role = new Role();
        role.setId(id);
        role.setName("USER");

        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(role));

        Role foundRole = roleService.findRoleByName("USER");

        assertEquals(id, foundRole.getId());
        assertEquals("USER", foundRole.getName());
    }

    @Test
    void givenName_whenFindRoleByName_throwException() {
        when(roleRepository.findRoleByName(any(String.class))).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.findRoleByName("USER"));
    }

    @Test
    void givenRole_whenAddRole_returnRole() {

        Role role = new Role();
        role.setId(getRandomLong());
        role.setName("USER");

        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role addedRole = roleService.addRole(role);

        assertEquals(role.getId(), addedRole.getId());
        assertEquals(role.getName(), addedRole.getName());
    }

    @Test
    void givenRole_whenAddRoleWithExistingName_throwException() {

        Role role = new Role();
        role.setId(getRandomLong());
        role.setName("USER");

        when(roleRepository.existsByName(role.getName())).thenReturn(true);

        assertThrows(RoleAlreadyExistsException.class, () -> roleService.addRole(role));
    }

    @Test
    void givenRole_whenUpdateRole_returnRole() {

        Long id = getRandomLong();
        Role role = new Role();
        role.setId(id);
        role.setName("USER");

        when(roleRepository.findById(any(Long.class))).thenReturn(Optional.of(new Role()));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role updatedRole = roleService.updateRole(id, role);
        assertEquals(id, updatedRole.getId());
        assertEquals(role.getName(), updatedRole.getName());
    }

    @Test
    void givenId_whenUpdateRole_throwException() {

        when(roleRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Long id = getRandomLong();
        Role role = new Role();
        role.setId(id);
        role.setName("USER");

        assertThrows(RoleNotFoundException.class,
                () -> roleService.updateRole(id, role));
    }

    @Test
    void givenRole_whenUpdateRoleWithExistingName_throwException() {

        Long id = getRandomLong();
        Role role = new Role();
        role.setId(id);
        role.setName("USER");

        when(roleRepository.findById(any(Long.class))).thenReturn(Optional.of(new Role()));
        when(roleRepository.existsByName(role.getName())).thenReturn(true);

        assertThrows(RoleAlreadyExistsException.class,
                () -> roleService.updateRole(id, role));
    }

    @Test
    void givenId_whenDeleteRole_doesNotThrowException() {

        when(roleRepository.existsById(any(Long.class))).thenReturn(true);

        assertDoesNotThrow(() -> roleService.deleteRole(getRandomLong()));
    }

    @Test
    void givenId_whenDeleteRole_throwException() {
        assertThrows(RoleNotFoundException.class, () -> roleService.deleteRole(getRandomLong()));
    }

    private Long getRandomLong() {
        return (long) new Random().ints(1, 10).findFirst().getAsInt();
    }


}
