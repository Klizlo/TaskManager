package com.example.TaskManager.controller;

import com.example.TaskManager.dto.RoleDto;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.repository.RoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenFindAllRoles_returnNonEmptyList() throws Exception {

        Role role = new Role();
        role.setName("Role1");

        roleRepository.save(role);

        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    public void givenRoleId_whenFindRole_returnRole() throws Exception {
        Role role = new Role();
        role.setName("Role2");

        Role savedRole = roleRepository.save(role);

        MvcResult mvcResult = mockMvc.perform(get("/api/roles/" + savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        RoleDto foundRole = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundRole.getId()).isEqualTo(savedRole.getId());
        assertThat(foundRole.getName()).isEqualTo(foundRole.getName());
    }

    @Test
    public void givenRoleName_whenFindRole_returnRole() throws Exception {
        Role role = new Role();
        role.setName("Role3");

        Role savedRole = roleRepository.save(role);

        MvcResult mvcResult = mockMvc.perform(get("/api/roles/" + savedRole.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        RoleDto foundRole = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundRole.getName()).isEqualTo(foundRole.getName());
        assertThat(foundRole.getId()).isEqualTo(savedRole.getId());
    }

    @Test
    public void givenNonExistingRoleId_whenFindRole_returnStatus400() throws Exception {

        mockMvc.perform(get("/api/roles/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Role id " + 1_000L + " does not exist")));
    }

    @Test
    public void givenNonExistingRoleName_whenFindRole_returnStatus400() throws Exception {

        mockMvc.perform(get("/api/roles/" + "NonExistingRole")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Role NonExistingRole does not exist")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenRole_whenAddRole_returnRole() throws Exception {
        Role role = new Role();
        role.setName("Role4");

        MvcResult mvcResult = mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        RoleDto addedRole = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(addedRole.getName()).isEqualTo(role.getName());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenRole_whenAddRoleByUnauthorizedUser_returnStatus403() throws Exception {
        Role role = new Role();
        role.setName("Role5");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenExistingRole_whenAddRole_returnStatus400() throws Exception {
        Role role = new Role();
        role.setName("Role6");

        roleRepository.save(role);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(role)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Role " + role.getName() + " already exists")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenRoleIdAndRole_whenUpdateRole_returnRole() throws Exception {

        Role role = new Role();
        role.setName("Role7");

        Role savedRole = roleRepository.save(role);

        savedRole.setName("Role7.1");

        MvcResult mvcResult = mockMvc.perform(put("/api/roles/" + savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedRole)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        RoleDto updatedRole = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedRole.getId()).isEqualTo(savedRole.getId());
        assertThat(updatedRole.getName()).isEqualTo(savedRole.getName());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenRoleIdAndRole_whenUpdateRoleByUnauthorizedUser_returnStatus403() throws Exception {

        Role role = new Role();
        role.setName("Role8");

        Role savedRole = roleRepository.save(role);

        savedRole.setName("Role8.1");

        mockMvc.perform(put("/api/roles/" + savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedRole)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenRoleIdAndRoleWithExistingName_whenUpdateRole_returnStatus400() throws Exception {

        Role role9_1 = new Role();
        role9_1.setName("Role9.1");

        Role savedRole = roleRepository.save(role9_1);

        Role role9_2 = new Role();
        role9_2.setName("Role9.2");

        roleRepository.save(role9_2);

        savedRole.setName("Role9.2");

        mockMvc.perform(put("/api/roles/" + savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedRole)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Role " + savedRole.getName() + " already exists")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenRoleId_whenDeleteRole_returnStatus200() throws Exception {

        Role role = new Role();
        role.setName("Role10");

        Role savedRole = roleRepository.save(role);

        mockMvc.perform(delete("/api/roles/" + savedRole.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenRoleId_whenDeleteRoleByUnauthorizedUser_returnStatus403() throws Exception {

        Role role = new Role();
        role.setName("Role11");

        Role savedRole = roleRepository.save(role);

        mockMvc.perform(delete("/api/roles/" + savedRole.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenNonExistingRoleId_whenDeleteRole_returnStatus400() throws Exception {

        mockMvc.perform(delete("/api/roles/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}
