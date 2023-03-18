package com.example.TaskManager.service;

import com.example.TaskManager.exception.UserAlreadyExistsException;
import com.example.TaskManager.exception.UserNotFoundException;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException(username));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public User addUser(User user) {

        if (userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(user.getEmail());
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UserAlreadyExistsException(user.getUsername());

        Role roleUser = roleService.findRoleByName("USER");

        user.addRole(roleUser);
        roleUser.addUser(user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {

        User foundUser = findUserById(id);

        if (!user.getEmail().equals(foundUser.getEmail()) && userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(user.getEmail());
        if (!user.getUsername().equals(foundUser.getUsername()) && userRepository.existsByUsername(user.getUsername()))
            throw new UserAlreadyExistsException(user.getUsername());

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());

        return userRepository.save(foundUser);
    }

    @Override
    @Transactional
    public User modifyUserRoles(Long userId, List<Long> roleIds) {

        User user = findUserById(userId);

        List<Role> roles = roleService.findAllRolesById(roleIds);

        Set<Role> rolesToRevoke = user.getRoles().stream()
                .filter(role -> !role.getName().equals("USER") && !roles.contains(role))
                .collect(Collectors.toSet());
        
        rolesToRevoke.forEach(role -> role.removeUser(user));
        user.getRoles().removeAll(rolesToRevoke);

        Set<Role> rolesToAssign = roles.stream()
                .filter(role -> !user.getRoles().contains(role))
                .collect(Collectors.toSet());

        rolesToAssign.forEach(role -> role.addUser(user));
        user.getRoles().addAll(rolesToAssign);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException(id);

        userRepository.deleteById(id);
    }
}
