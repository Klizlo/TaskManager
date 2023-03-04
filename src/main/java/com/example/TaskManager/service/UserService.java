package com.example.TaskManager.service;

import com.example.TaskManager.exception.UserAlreadyExistsException;
import com.example.TaskManager.exception.UserNotExistsException;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotExistsException(id));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotExistsException(email));
    }

    @Override
    @Transactional
    public User addUser(User user) {

        if (userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {

        User foundUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistsException(id));

        if (userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(user.getEmail());

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());

        return userRepository.save(foundUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotExistsException(id));

        userRepository.deleteById(id);
    }
}
