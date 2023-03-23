package com.example.TaskManager.service;

import com.example.TaskManager.exception.TaskNotFoundException;
import com.example.TaskManager.exception.UserNotFoundException;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.TaskRepository;
import com.example.TaskManager.security.service.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private TaskService taskService;

    @Test
    void whenFindAllTask_returnEmptyList(){

        List<Task> allTasks = taskService.findAllTasks();
        assertTrue(allTasks.isEmpty());
    }

    @Test
    void whenFindAllTask_returnNonEmptyList(){

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> allTasks = taskService.findAllTasks();
        assertFalse(allTasks.isEmpty());
        assertEquals(1, allTasks.size());
    }

    @Test
    void givenId_whenFindTaskById_returnTask(){
        Long taskId = getRandomLong();

        Task task = new Task();
        task.setId(taskId);
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Task foundTask = taskService.findTask(taskId);

        assertEquals(foundTask.getId(), taskId);
        assertEquals(foundTask.getName(), task.getName());
        assertEquals(foundTask.getPriority(), task.getPriority());
    }

    @Test
    void givenId_whenFindTaskById_throwTaskNotFoundException(){
        Long taskId = getRandomLong();

        when(taskRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.findTask(taskId));
    }

    @Test
    void givenUserIdAndTask_whenAddTask_returnTask() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");
        user.setEmail("user@example.com");

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setOwner(user);

        when(userService.findUserById(user.getId())).thenReturn(user);
        when(taskRepository.save(any())).thenReturn(task);

        Task addedTask = taskService.addTask(task);

        assertEquals(task.getName(), addedTask.getName());
        assertEquals(task.getPriority(), addedTask.getPriority());
        assertNotNull(addedTask.getOwner());
        assertEquals(user, addedTask.getOwner());
    }

    @Test
    void givenUserIdAndTask_whenAddTask_throwUserNotFoundException() {

        Long userId = getRandomLong();

        User user = new User();
        user.setId(userId);

        Task task = new Task();
        task.setOwner(user);

        when(userService.findUserById(userId)).thenThrow(new UserNotFoundException(userId));

        assertThrows(UserNotFoundException.class, () -> taskService.addTask(task));
    }

    @Test
    void givenTaskIdAndTask_whenUpdateTask_returnTask() {

        User user = new User();
        user.setId(1L);
        user.setUsername("User");
        user.setEmail("user@example.com");

        Long id = getRandomLong();

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setOwner(user);

        user.getTasks().add(task);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "User", "user@example.com",
                "User123#", List.of(new SimpleGrantedAuthority("USER")));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userService.findUserByUsername(any())).thenReturn(user);

        Task editedTask = task;
        editedTask.setPriority(Task.Priority.MEDIUM);
        editedTask.setName("New task");

        Task updatedTask = taskService.updateTask(id, editedTask);

        assertEquals(editedTask.getName(), updatedTask.getName());
        assertEquals(editedTask.getPriority(), updatedTask.getPriority());
    }

    @Test
    void givenTaskIdAndTask_whenUpdateTask_throwTaskNotFoundException() {

        Long id = getRandomLong();

        Task task = new Task();
        task.setName("New task");
        task.setPriority(Task.Priority.LOW);

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(id, task));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    void givenTaskId_whenDeleteTask_doesNotThrowException() {

        User user = new User();
        user.setId(1L);
        user.setUsername("User");
        user.setEmail("user@example.com");

        Long id = getRandomLong();

        Task task = new Task();
        task.setId(id);
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setOwner(user);

        user.getTasks().add(task);

        when(taskRepository.existsById(any())).thenReturn(true);

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "User", "user@example.com",
                "User123#", List.of(new SimpleGrantedAuthority("USER")));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        when(userService.findUserByUsername(any())).thenReturn(user);

        assertDoesNotThrow(() -> taskService.deleteTask(task.getId()));
    }

    @Test
    void givenTaskId_whenDeleteTask_throwException() {

        when(taskRepository.existsById(any())).thenReturn(false);

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(getRandomLong()));
    }

    private Long getRandomLong() {
        return (long) new Random().ints(0, 10).findFirst().getAsInt();
    }

}
