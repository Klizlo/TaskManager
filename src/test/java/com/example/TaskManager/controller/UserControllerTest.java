package com.example.TaskManager.controller;

import com.example.TaskManager.dto.RoleDto;
import com.example.TaskManager.dto.UserDto;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import com.example.TaskManager.service.IRoleService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    public static void addUsers(@Autowired UserRepository userRepository,
                                @Autowired IRoleService roleService){
        User admin = new User();
        admin.setUsername("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("Admin123#");

        Role adminRole = roleService.findRoleByName("ADMIN");
        adminRole.addUser(admin);
        admin.addRole(adminRole);

        userRepository.save(admin);

        User user = new User();
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        userRepository.save(user);
    }

    @Test
    public void whenFindAllUsers_returnNonEmptyList() throws Exception {
        User user = new User();
        user.setUsername("Adam");
        user.setEmail("adam@example.com");
        user.setPassword(passwordEncoder.encode("Adam123#"));
        
        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        List<UserDto> users = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(users.get(users.size()-1).getUsername()).isEqualTo(user.getUsername());
        assertThat(users.get(users.size()-1).getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenUser_whenAddUser_returnUser() throws Exception {
        User user = new User();
        user.setUsername("Elizabeth");
        user.setEmail("elizabeth@example.com");
        user.setPassword("Elizabeth123#");

        MvcResult mvcResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        UserDto addedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(addedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(addedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(addedUser.getRoles().size()).isEqualTo(1);
        assertThat(addedUser.getRoles().stream().map(RoleDto::getName).anyMatch(role -> role.equals("USER"))).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void  givenUserWithRoles_whenAddUser_returnUser() throws Exception {
        User user = new User();
        user.setUsername("Jane");
        user.setEmail("jane@example.com");
        user.setPassword("Jane123#");

        Role admin = roleService.findRoleByName("ADMIN");
        user.addRole(admin);

        MvcResult mvcResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        UserDto addedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(addedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(addedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(addedUser.getRoles().size()).isEqualTo(2);
        assertThat(addedUser.getRoles().stream().map(RoleDto::getName).anyMatch(role -> role.equals("ADMIN"))).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void  givenUser_whenAddUserByUnauthorizedUser_throwStatus403() throws Exception {
        User user = new User();
        user.setUsername("Natalie");
        user.setEmail("natalie@example.com");
        user.setPassword("Natalie123#");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void  givenUserWithExistingEmail_whenAddUser_returnStatus400() throws Exception {

        User amy = new User();
        amy.setUsername("Amy");
        amy.setEmail("amy@example.com");
        amy.setPassword("Amy1234#");

        userRepository.save(amy);

        User amy1 = new User();
        amy1.setUsername("Amy1");
        amy1.setEmail("amy@example.com");
        amy1.setPassword("Amy1234#");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amy1)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User already exists with:")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void  givenUserWithExistingUsername_whenAddUser_returnStatus400() throws Exception {

        User millie = new User();
        millie.setUsername("Millie");
        millie.setEmail("millie@example.com");
        millie.setPassword("Millie123#");

        userRepository.save(millie);

        User millie1 = new User();
        millie1.setUsername("Millie");
        millie1.setEmail("millie1@example.com");
        millie1.setPassword("Millie123#");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(millie1)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User already exists with:")));
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"}, username = "Admin")
    void givenUserIdAndUser_whenUpdateUser_returnUser() throws Exception {
        User user = new User();
        user.setUsername("Chris");
        user.setEmail("chris@example.com");
        user.setPassword("Chris123#");

        Role userRole = roleService.findRoleByName("USER");
        userRole.addUser(user);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        savedUser.setUsername("Chris123");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"}, username = "Admin")
    void givenUserIdAndUserWithRoles_whenUpdateUser_returnUser() throws Exception {
        User user = new User();
        user.setUsername("Johnny");
        user.setEmail("johnny@example.com");
        user.setPassword("Johnny123#");

        Role userRole = roleService.findRoleByName("USER");
        userRole.addUser(user);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        Role adminRole = roleService.findRoleByName("ADMIN");
        savedUser.addRole(adminRole);

        savedUser.setUsername("Johnny123");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(updatedUser.getRoles().stream().map(RoleDto::getName).anyMatch(role -> role.equals("ADMIN"))).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "Dave")
    void givenUserIdAndUser_whenUpdateUserByAccountOwner_returnUser() throws Exception {
        User user = new User();
        user.setUsername("Dave");
        user.setEmail("dave@example.com");
        user.setPassword("Dave123#");

        Role userRole = roleService.findRoleByName("USER");
        userRole.addUser(user);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        savedUser.setUsername("Dave123");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(updatedUser.getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    void givenUserIdAndUser_whenUpdateUserByUnauthorizedUser_returnStatus403() throws Exception {
        User user = new User();
        user.setUsername("Albert");
        user.setEmail("albert@example.com");
        user.setPassword("Albert123#");

        Role userRole = roleService.findRoleByName("USER");
        userRole.addUser(user);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        savedUser.setUsername("Albert123");

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "Barry")
    void givenUserIdAndUserWithRoles_whenUpdateUserByNonAdmin_returnUserWithPermissionUnchanged() throws Exception {
        User user = new User();
        user.setUsername("Barry");
        user.setEmail("barry@example.com");
        user.setPassword("Barry123#");

        Role userRole = roleService.findRoleByName("USER");
        userRole.addUser(user);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        Role adminRole = roleService.findRoleByName("ADMIN");
        savedUser.addRole(adminRole);

        savedUser.setUsername("Barry123");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedUser.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(updatedUser.getRoles().size()).isEqualTo(1);
        assertThat(updatedUser.getRoles().stream().map(RoleDto::getName).noneMatch(role -> role.equals("ADMIN"))).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    void givenUserIdAndUser_whenUpdateNonExistingUser_returnStatus400() throws Exception {
        User user = new User();
        user.setUsername("Example");
        user.setEmail("example@example.com");

        long userId = 1_000L;

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("User id " + userId + " does not exist")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "Peter")
    public void givenIdAndUserWithExistingUsername_whenUpdateUser_returnStatus400() throws Exception {

        Role role = roleService.findRoleByName("USER");

        User peter = new User();
        peter.setUsername("Peter");
        peter.setEmail("peter@example.com");
        peter.setPassword("Peter123#");

        role.addUser(peter);
        peter.addRole(role);

        User savedPeter = userRepository.save(peter);

        User peter1 = new User();
        peter1.setUsername("Peter1");
        peter1.setEmail("peter1@example.com");
        peter1.setPassword("Peter123#");

        role.addUser(peter1);
        peter1.addRole(role);

        userRepository.save(peter1);

        savedPeter.setUsername("Peter1");

        mockMvc.perform(put("/api/users/" + savedPeter.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedPeter)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User already exists with:")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "William")
    public void givenIdAndUserWithExistingEmail_whenUpdateUser_returnStatus400() throws Exception {

        Role role = roleService.findRoleByName("USER");

        User william = new User();
        william.setUsername("William");
        william.setEmail("william@example.com");
        william.setPassword("William123#");

        role.addUser(william);
        william.addRole(role);

        User savedWilliam = userRepository.save(william);

        User william1 = new User();
        william1.setUsername("William1");
        william1.setEmail("william1@example.com");
        william1.setPassword("William123#");

        role.addUser(william1);
        william1.addRole(role);

        userRepository.save(william1);

        savedWilliam.setEmail("william1@example.com");

        mockMvc.perform(put("/api/users/" + savedWilliam.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(savedWilliam)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User already exists with:")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenUserIdAndRoleIds_whenModifyUserRoles_returnUser() throws Exception {

        User user = new User();
        user.setUsername("Marie");
        user.setEmail("marie@example.com");
        user.setPassword("Marie123#");

        Role userRole = roleService.findRoleByName("USER");
        user.addRole(userRole);
        userRole.addUser(user);

        User savedUser = userRepository.save(user);

        Role adminRole = roleService.findRoleByName("ADMIN");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + savedUser.getId() + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(adminRole.getId()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        UserDto modifiedUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(modifiedUser.getRoles().size()).isEqualTo(2);
        assertThat(modifiedUser.getRoles().stream()
                .map(RoleDto::getName)
                .anyMatch(role -> role.equals("ADMIN"))).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenUserIdAndRoleIds_whenModifyUserRolesByUnauthorizedUser_returnStatus403() throws Exception {

        mockMvc.perform(put("/api/users/" + 1_000L + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenUserId_whenDeleteUserByAdmin_returnStatus200() throws Exception {
        User user = new User();
        user.setUsername("Thomas");
        user.setEmail("thomas@example.com");
        user.setPassword(passwordEncoder.encode("Thomas123#"));

        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(savedUser.getId())).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "George")
    public void givenUserId_whenDeleteUserByAccountOwner_returnStatus200() throws Exception {
        User user = new User();
        user.setUsername("George");
        user.setEmail("george@example.com");
        user.setPassword(passwordEncoder.encode("George123#"));

        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(savedUser.getId())).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenUserId_whenDeleteUserByUnauthorizedUser_returnStatus403() throws Exception {
        User user = new User();
        user.setUsername("John");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("John123#"));

        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenUserId_whenDeleteUserByNotLoggedUser_returnStatus401() throws Exception {
        User user = new User();
        user.setUsername("Mark");
        user.setEmail("mark@example.com");
        user.setPassword(passwordEncoder.encode("Mark123#"));

        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}