package com.example.TaskManager.service;

import com.example.TaskManager.config.TestConfig;
import com.example.TaskManager.exception.UserAlreadyExistsException;
import com.example.TaskManager.exception.UserNotExistsException;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import({TestConfig.class})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void whenFindingUsers_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> users = userService.findAllUsers();

        assertTrue(users.isEmpty());
    }

    @Test
    void whenFindingUsers_thenReturnNonEmptyList() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.findAllUsers();

        assertFalse(users.isEmpty());
        assertEquals("Example", users.get(0).getUsername());
        assertEquals(user.getId(), users.get(0).getId());
    }

    @Test
    void givenUserId_whenFindUserById_ReturnUser() {

        Long id = getRandomLong();
        User user = new User();
        user.setId(id);
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(id);

        assertEquals(id, foundUser.getId());
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void givenUserId_whenFindUserById_throwException() {

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Long id = getRandomLong();

        assertThrows(UserNotExistsException.class, () -> userService.findUserById(id));
    }

    @Test
    void givenUsername_whenFindUserByEmail_returnUser() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByEmail(user.getEmail());

        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getUsername(), foundUser.getUsername());

    }

    @Test
    void givenUsername_whenFindUserByEmail_throwException() {

        String username = "Example";

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> userService.findUserByEmail(username));

    }

    @Test
    void givenUser_whenAddUser_returnUser() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");
        user.setPassword("Example123");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User addedUser = userService.addUser(user);

        assertEquals(user.getId(), addedUser.getId());
        assertEquals(user.getUsername(), addedUser.getUsername());
        assertEquals(user.getPassword(), addedUser.getPassword());
    }

    @Test
    void givenUser_whenAddUserWithExistingEmail_throwException() {

        User user = new User();
        user.setEmail("example@example.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(user));

    }

    @Test
    void givenUser_whenUpdateUser_returnUser() {

        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(id, user);

        assertEquals(user.getUsername(), updatedUser.getUsername());
        assertEquals(user.getEmail(), updatedUser.getEmail());

    }

    @Test
    void givenId_whenUpdateUser_throwException() {
        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void givenUser_whenUpdateUser_throwException() {
        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void givenUserId_whenDeleteUser_doesNotThrowException() {

    when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(new User()));

    assertDoesNotThrow(() -> userService.deleteUser(getRandomLong()));

    }

    @Test
    void givenUserId_whenDeleteUser_throwException() {

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(UserNotExistsException.class, () -> userService.deleteUser(getRandomLong()));

    }

    private Long getRandomLong() {
        return (long) new Random().ints(1, 10).findFirst().getAsInt();
    }
}