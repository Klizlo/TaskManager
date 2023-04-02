package com.example.TaskManager.controller;

import com.example.TaskManager.dto.CategoryDto;
import com.example.TaskManager.model.Category;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.CategoryRepository;
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
@AutoConfigureTestDatabase
@TestPropertySource(locations = "classpath:application-test.properties")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private IUserService userService;
    @Autowired
    private ObjectMapper objectMapper;

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
    public void whenFindAllCategoriesByAdmin_returnCategories() throws Exception {
        Category category = new Category();
        category.setName("Home");
        User user = userService.findUserByUsername("User");
        category.setOwner(user);
        categoryRepository.save(category);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        List<CategoryDto> categories = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(categories.get(categories.size()-1).getName()).isEqualTo(category.getName());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void whenFindAllCategoriesByUnauthorizedUser_returnStatus403() throws Exception {

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenFindCategory_returnCategory() throws Exception {
        Category category = new Category();
        category.setName("Job");
        User user = userService.findUserByUsername("User");
        category.setOwner(user);
        Category searchedCategory = categoryRepository.save(category);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/" + searchedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto foundCategory = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.getId()).isEqualTo(searchedCategory.getId());
        assertThat(foundCategory.getName()).isEqualTo(category.getName());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN","USER"}, username = "Admin")
    public void givenId_whenFindCategoryByAdmin_returnCategory() throws Exception {
        Category category = new Category();
        category.setName("School");
        User user = userService.findUserByUsername("User");
        category.setOwner(user);
        Category searchedCategory = categoryRepository.save(category);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/" + searchedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto foundCategory = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.getId()).isEqualTo(searchedCategory.getId());
        assertThat(foundCategory.getName()).isEqualTo(category.getName());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenFindCategoryByUnauthorizedUser_returnStatus403() throws Exception {
        Category category = new Category();
        category.setName("School");
        User user = userService.findUserByUsername("Admin");
        category.setOwner(user);
        Category searchedCategory = categoryRepository.save(category);

        mockMvc.perform(get("/api/categories/" + searchedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenFindNotExistingCategory_returnStatus400() throws Exception {
        mockMvc.perform(get("/api/categories/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Category id " + 1_000L + " not found")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenCategory_whenAddCategory_returnCategory() throws Exception {
        Category category = new Category();
        category.setName("Pet");
        User user = userService.findUserByUsername("User");
        category.setOwner(user);

        MvcResult mvcResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        CategoryDto addedCategory = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(addedCategory.getName()).isEqualTo(category.getName());
        assertThat(addedCategory.getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenCategoryWithExistingNameForUser_whenAddCategory_returnStatus400() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setName("Shopping");
        User user = userService.findUserByUsername("User");
        existingCategory.setOwner(user);
        categoryRepository.save(existingCategory);

        Category category = new Category();
        category.setName("Shopping");
        category.setOwner(user);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Category already exists with: " + category.getName())));
    }

    @Test
    public void givenCategory_whenAddCategoryByNotLoggedUser_returnStatus401() throws Exception {
        Category category = new Category();
        category.setName("Category");
        User user = userService.findUserByUsername("User");
        category.setOwner(user);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenIdAndCategory_whenUpdateCategory_returnCategory() throws Exception {
        Category category = new Category();
        category.setName("Hobby");

        User user = userService.findUserByUsername("User");
        category.setOwner(user);

        Category savedCategory = categoryRepository.save(category);

        savedCategory.setName("Board Games");

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedCategory)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto updatedCategory = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedCategory.getId()).isEqualTo(savedCategory.getId());
        assertThat(updatedCategory.getName()).isEqualTo(savedCategory.getName());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenIdAndCategory_whenUpdateCategoryByAdmin_returnCategory() throws Exception {
        Category category = new Category();
        category.setName("Birthday");

        User user = userService.findUserByUsername("User");
        category.setOwner(user);

        Category savedCategory = categoryRepository.save(category);

        savedCategory.setName("Birthdays");

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedCategory)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto updatedCategory = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {});

        assertThat(updatedCategory.getId()).isEqualTo(savedCategory.getId());
        assertThat(updatedCategory.getName()).isEqualTo(savedCategory.getName());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenIdAndCategory_whenUpdateCategoryByUnauthorizedUser_returnStatus403() throws Exception {
        Category category = new Category();
        category.setName("Hobby");

        User user = userService.findUserByUsername("Admin");
        category.setOwner(user);

        Category savedCategory = categoryRepository.save(category);

        savedCategory.setName("Board Games");

        mockMvc.perform(put("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedCategory)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenIdAndCategory_whenUpdateNonExistingCategory_returnStatus400() throws Exception {
        Category category = new Category();
        category.setName("Hobby");

        User user = userService.findUserByUsername("User");
        category.setOwner(user);

        mockMvc.perform(put("/api/categories/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(category)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Category id " + 1_000L + " not found")));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenIdAndCategoryWithExistingNameForUser_whenUpdateNCategory_returnStatus400() throws Exception {
        Category programing = new Category();
        programing.setName("Programing");

        User user = userService.findUserByUsername("User");
        programing.setOwner(user);

        categoryRepository.save(programing);

        Category category = new Category();
        category.setName("Java");
        category.setOwner(user);
        Category savedCategory = categoryRepository.save(category);

        savedCategory.setName("Programing");

        mockMvc.perform(put("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(savedCategory)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Category already exists with: "
                        + savedCategory.getName())));
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenDeleteCategory_returnStatus200() throws Exception {
        Category category = new Category();
        category.setName("Holidays");

        User user = userService.findUserByUsername("User");
        category.setOwner(user);
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN", "USER"}, username = "Admin")
    public void givenId_whenDeleteCategoryByAdmin_returnStatus200() throws Exception {
        Category category = new Category();
        category.setName("Friends");

        User user = userService.findUserByUsername("User");
        category.setOwner(user);
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenDeleteCategoryByUnauthorizedUser_returnStatus403() throws Exception {
        Category category = new Category();
        category.setName("Friends");

        User user = userService.findUserByUsername("Admin");
        category.setOwner(user);
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"}, username = "User")
    public void givenId_whenDeleteNonExistingCategory_returnStatus400() throws Exception {
        mockMvc.perform(delete("/api/categories/" + 1_000L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Category id " + 1_000L + " not found")));
    }

}
