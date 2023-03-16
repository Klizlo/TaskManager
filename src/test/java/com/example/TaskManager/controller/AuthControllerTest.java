package com.example.TaskManager.controller;

import com.example.TaskManager.controller.credentials.SignInCredentials;
import com.example.TaskManager.controller.credentials.SignUpCredentials;
import com.example.TaskManager.dto.JwtTokenDto;
import com.example.TaskManager.dto.RoleDto;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.UserRepository;
import com.example.TaskManager.service.IRoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenSignInCredentials_whenSignIn_returnToken() throws Exception {

        User user = new User();
        user.setUsername("Thomas");
        user.setEmail("thomas@example.com");
        user.setPassword(passwordEncoder.encode("Thomas123#"));

        Role role = roleService.findRoleByName("USER");

        user.getRoles().add(role);
        role.getUsers().add(user);

        userRepository.saveAndFlush(user);

        SignInCredentials credentials = new SignInCredentials();
        credentials.setUsername("Thomas");
        credentials.setPassword("Thomas123#");

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        JwtTokenDto token = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<JwtTokenDto>() {
                });

        assertThat(token.getJwt()).isNotNull();
        assertThat(token.getUser().getUsername()).isEqualTo("Thomas");
        assertThat(token.getUser().getRoles().size()).isGreaterThan(0);
        assertThat(token.getUser().getRoles().stream()
                .map(RoleDto::getName).toList().contains("USER")).isTrue();
    }

    @Test
    public void givenInvalidSigInCredentials_whenSignIn_returnStatus401() throws Exception {

        SignInCredentials credentials = new SignInCredentials();
        credentials.setUsername("Adam");
        credentials.setPassword("Adam123#");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void givenSignUpCredentials_whenSignUp_returnToken() throws Exception {

        SignUpCredentials credentials = new SignUpCredentials();
        credentials.setUsername("Natalie");
        credentials.setEmail("natalie@example.com");
        credentials.setPassword("Natalie123#");

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JwtTokenDto token = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<JwtTokenDto>() {});

        assertThat(token.getJwt()).isNotNull();
        assertThat(token.getUser().getUsername()).isEqualTo(credentials.getUsername());
        assertThat(token.getUser().getEmail()).isEqualTo(credentials.getEmail());
        assertThat(token.getUser().getRoles().stream().map(RoleDto::getName).toList().contains("USER")).isTrue();
    }

    @Test
    public void givenInvalidSignUpCredentials_whenSignUp_returnStatus403() throws Exception {

        SignUpCredentials credentials = new SignUpCredentials();
        credentials.setUsername("Jennifer");
        credentials.setEmail("jenniferexample.com");
        credentials.setPassword("Jennifer");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", hasSize(equalTo(2))));

    }

    @Test
    public void givenSignUpCredentials_whenSignUpWithExistingData_returnStatus401() throws Exception {

        User user = new User();
        user.setUsername("Maria");
        user.setEmail("maria@example.com");
        user.setPassword(passwordEncoder.encode("Maria123#"));

        userRepository.save(user);

        SignUpCredentials credentials = new SignUpCredentials();
        credentials.setUsername(user.getUsername());
        credentials.setEmail(user.getEmail());
        credentials.setPassword("Maria123#");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("User already exists with: " + credentials.getEmail())));

        credentials.setEmail("maria1@example.com");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("User already exists with: " + credentials.getUsername())));
    }
}
