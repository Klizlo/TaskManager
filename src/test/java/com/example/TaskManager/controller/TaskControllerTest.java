package com.example.TaskManager.controller;

import com.example.TaskManager.dto.TaskDto;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.TaskRepository;
import com.example.TaskManager.service.IRoleService;
import com.example.TaskManager.service.IUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private IUserService userService;

    @BeforeAll
    public static void addUsers(@Autowired IUserService userService,
                                 @Autowired IRoleService roleService) {
        User admin = new User();
        admin.setUsername("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("Admin123#");

        Role adminRole = roleService.findRoleByName("ADMIN");

        admin = userService.addUser(admin);
        userService.modifyUserRoles(admin.getId(), List.of(adminRole.getId()));

        User user = new User();
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        userService.addUser(user);
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void whenFindAllTasks_returnNonEmptyList() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void whenFindAllTasksByUnauthorizedUser_returnStatus403() throws Exception {

        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenFindTask_returnTask() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        MvcResult mvcResult = mockMvc.perform(get("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TaskDto foundTask = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getName()).isEqualTo(savedTask.getName());
        assertThat(foundTask.getPriority()).isEqualTo(savedTask.getPriority());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenId_whenFindTaskByAdmin_returnTask() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        MvcResult mvcResult = mockMvc.perform(get("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TaskDto foundTask = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getName()).isEqualTo(savedTask.getName());
        assertThat(foundTask.getPriority()).isEqualTo(savedTask.getPriority());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenFindTaskByUnauthorizedUser_returnStatus403() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("Admin");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenNonExistingId_whenFindTask_returnStatus400() throws Exception {

        mockMvc.perform(get("/api/tasks/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Task id 1000 not found")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenTask_whenAddTask_returnTask() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        MvcResult mvcResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        TaskDto addedTask = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(addedTask.getName()).isEqualTo(task.getName());
        assertThat(addedTask.getPriority()).isEqualTo(task.getPriority());
        assertThat(addedTask.getOwner().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void givenTask_whenAddTaskByNotLoggedUser_returnStatus401() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDateTime.now());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenTaskIdAndTask_whenUpdateTask_returnTask() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);
        savedTask.setPriority(Task.Priority.MEDIUM);

        MvcResult mvcResult = mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedTask)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TaskDto updatedTask = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedTask.getPriority()).isEqualTo(savedTask.getPriority());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenTaskIdAndTask_whenUpdateTaskByAdmin_returnTask() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);
        savedTask.setPriority(Task.Priority.MEDIUM);

        MvcResult mvcResult = mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedTask)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TaskDto updatedTask = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedTask.getPriority()).isEqualTo(savedTask.getPriority());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenTaskIdAndTask_whenUpdateTaskByUnauthorizedUser_returnStatus403() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("Admin");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);
        savedTask.setPriority(Task.Priority.MEDIUM);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedTask)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenNonExistingTaskIdAndTask_whenUpdateTask_returnStatus400() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        mockMvc.perform(put("/api/tasks/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Task id 1000 not found")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenTaskId_whenDeleteTask_returnStatus200() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenTaskId_whenDeleteTaskByAdmin_returnStatus200() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("User");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenTaskId_whenDeleteTaskByUnauthorizedUser_returnStatus403() throws Exception {

        Task task = new Task();
        task.setName("Task");
        task.setPriority(Task.Priority.LOW);
        task.setDeadline(LocalDateTime.now());

        User user = userService.findUserByUsername("Admin");

        task.setOwner(user);

        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenNonExistingTaskId_whenDeleteTask_returnStatus400() throws Exception {

        mockMvc.perform(delete("/api/tasks/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Task id 1000 not found")));
    }

}
