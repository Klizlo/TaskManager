package com.example.TaskManager.service;

import com.example.TaskManager.config.TestConfig;
import com.example.TaskManager.exception.UserAlreadyExistsException;
import com.example.TaskManager.exception.UserNotFoundException;
import com.example.TaskManager.model.Category;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import({TestConfig.class})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
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

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(id));
    }

    @Test
    void givenUsername_whenFindUserByUsername_returnUser() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByUsername(user.getUsername());

        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());

    }

    @Test
    void givenUsername_whenFindUserByUsername_throwException() {

        String username = "Example";

        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserByUsername(username));

    }

    @Test
    void givenEmail_whenFindUserByEmail_returnUser() {

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
    void givenEmail_whenFindUserByEmail_throwException() {

        String email = "example@example.com";

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(email));

    }

    @Test
    void givenUserId_whenFindTasksByUser_returnListOfTasks() {
        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);

        when(userRepository.findTasksByUser(any())).thenReturn(List.of(task));

        List<Task> tasks = userService.findTasksByUser(getRandomLong());

        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
    }

    @Test
    void givenUserId_whenFindCategoriesByUser_returnListOfCategories() {
        Category category = new Category();
        category.setName("Category");

        when(userRepository.findCategoriesByUser(any())).thenReturn(List.of(category));

        List<Category> categories = userService.findCategoriesByUser(getRandomLong());

        assertFalse(categories.isEmpty());
        assertEquals(1, categories.size());
    }

    @Test
    void givenUser_whenAddUser_returnUser() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");
        user.setPassword("Example123");

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleService.findRoleByName("USER")).thenReturn(new Role());

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
    void givenUser_whenAddUserWithExistingUsername_throwException() {

        User user = new User();
        user.setUsername("Example");

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

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
    void givenId_whenUpdateUser_throwUserNotFoundException() {
        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void givenUserWithExistingEmail_whenUpdateUser_throwUserAlreadyExistsException() {
        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void givenUserWithExistingUsername_whenUpdateUser_throwUserAlreadyExistsException() {
        Long id = getRandomLong();

        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(id, user));
    }

    @Test
    void givenUserIdAndRoleIds_whenModifyUserRoles_returnUser() {

        Role roleUser = new Role();
        roleUser.setId(getRandomLong());
        roleUser.setName("USER");

        Role roleStaff = new Role();
        roleStaff.setId(10+getRandomLong());
        roleStaff.setName("STAFF");

        Role roleAdmin = new Role();
        roleAdmin.setId(20+getRandomLong());
        roleAdmin.setName("ADMIN");

        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roleUser);
        userRoles.add(roleAdmin);

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");
        user.setRoles(userRoles);

        List<Role> newUserRoles = List.of(roleUser, roleStaff);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(roleService.findAllRolesById(any())).thenReturn(newUserRoles);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User editedUser = userService.modifyUserRoles(user.getId(),
                newUserRoles.stream().map(Role::getId).toList());

        assertEquals(2, editedUser.getRoles().size());
        assertFalse(editedUser.getRoles().contains(roleAdmin));
        assertTrue(editedUser.getRoles().contains(roleStaff));
    }

    @Test
    void givenUserIdAndEmptyRoleIds_whenModifyUserRoles_returnUser() {

        Role roleUser = new Role();
        roleUser.setId(getRandomLong());
        roleUser.setName("USER");

        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roleUser);

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("Example");
        user.setEmail("example@example.com");
        user.setRoles(userRoles);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User editedUser = userService.modifyUserRoles(user.getId(), List.of());

        assertEquals(1, editedUser.getRoles().size());
        assertTrue(editedUser.getRoles().contains(roleUser));
    }

    @Test
    void givenNotExistingUserId_whenModifyUserRoles_throwException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Long userId = getRandomLong();

        assertThrows(UserNotFoundException.class, () -> userService.modifyUserRoles(userId, List.of(getRandomLong())));
    }

    @Test
    void givenUserId_whenDeleteUser_doesNotThrowException() {

    when(userRepository.existsById(any(Long.class))).thenReturn(true);

    assertDoesNotThrow(() -> userService.deleteUser(getRandomLong()));

    }

    @Test
    void givenUserId_whenDeleteUser_throwException() {

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(getRandomLong()));

    }

    private Long getRandomLong() {
        return (long) new Random().ints(1, 10).findFirst().getAsInt();
    }
}