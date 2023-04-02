package com.example.TaskManager.service;

import com.example.TaskManager.exception.CategoryAlreadyExistsException;
import com.example.TaskManager.exception.CategoryNotFoundException;
import com.example.TaskManager.model.Category;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.CategoryRepository;
import com.example.TaskManager.security.service.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void whenFindAllCategories_returnEmptyList() {

        List<Category> categories = categoryService.findAllCategories();

        assertTrue(categories.isEmpty());
    }

    @Test
    void whenFindAllCategories_returnNonEmptyList() {

        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Category");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> categories = categoryService.findAllCategories();

        assertFalse(categories.isEmpty());
        assertEquals(category.getId(), categories.get(0).getId());
        assertEquals(category.getName(), categories.get(0).getName());
    }

    @Test
    void givenId_whenFindCategoryById_returnCategory() {

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Category");
        category.setOwner(user);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(), user.getUsername(),
                user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        when(userService.findUserByUsername(any())).thenReturn(user);

        Category foundCategory = categoryService.findCategoryById(category.getId());

        assertEquals(category.getId(), foundCategory.getId());
        assertEquals(category.getName(), foundCategory.getName());
    }

    @Test
    void givenId_whenFindCategoryById_throwCategoryNotFoundException() {

        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findCategoryById(getRandomLong()));
    }

    @Test
    void givenCategory_whenAddCategory_returnCategory() {
        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Task");

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");

        category.setOwner(user);

        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryRepository.existsByNameAndOwnerId(category.getName(), user.getId())).thenReturn(false);
        when(userService.findUserById(user.getId())).thenReturn(user);

        Category addedCategory = categoryService.addCategory(category);

        assertEquals(category.getId(), addedCategory.getId());
        assertEquals(category.getName(), addedCategory.getName());
        assertEquals(user, addedCategory.getOwner());
    }

    @Test
    void givenExistingCategory_whenAddCategory_throwsCategoryAlreadyExistsException() {
        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Task");

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");

        category.setOwner(user);

        when(categoryRepository.existsByNameAndOwnerId(category.getName(), user.getId())).thenReturn(true);
        when(userService.findUserById(user.getId())).thenReturn(user);

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.addCategory(category));
    }

    @Test
    void givenIdAndCategory_whenUpdateCategory_returnCategory() {
        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Task");
        category.setOwner(user);

        Category categoryToEdit = new Category();
        categoryToEdit.setId(category.getId());
        categoryToEdit.setName("New task name");
        categoryToEdit.setOwner(category.getOwner());

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryRepository.existsByNameAndOwnerId(categoryToEdit.getName(), user.getId())).thenReturn(false);

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(), user.getUsername(),
                user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        when(userService.findUserByUsername(any())).thenReturn(user);

        Category editedCategory = categoryService.updateCategory(category.getId(), categoryToEdit);

        assertEquals(category.getId(), editedCategory.getId());
        assertEquals(category.getName(), editedCategory.getName());
        assertEquals(user, editedCategory.getOwner());
    }

    @Test
    void givenIdAndExistingCategory_whenUpdateCategory_throwsCategoryAlreadyExistsException() {
        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Task");
        category.setOwner(user);

        Category categoryToEdit = new Category();
        categoryToEdit.setId(category.getId());
        categoryToEdit.setName("Task");
        categoryToEdit.setOwner(category.getOwner());

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndOwnerId(category.getName(), user.getId())).thenReturn(true);

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(), user.getUsername(),
                user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        when(userService.findUserByUsername(any())).thenReturn(user);

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.updateCategory(category.getId(), categoryToEdit));
    }

    @Test
    void givenIdAndCategory_whenUpdateNonExistingCategory_throwsCategoryNotFoundException() {
        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Task");

        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");

        category.setOwner(user);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(category.getId(), category));
    }

    @Test
    void givenId_whenDeleteCategory_doesNotThrowException() {
        User user = new User();
        user.setId(getRandomLong());
        user.setUsername("User");
        user.setEmail("user@example.com");
        user.setPassword("User123#");

        Category category = new Category();
        category.setId(getRandomLong());
        category.setName("Category");

        user.getCategories().add(category);

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(), user.getUsername(),
                user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
        when(userService.findUserByUsername(any())).thenReturn(user);

        when(categoryRepository.existsById(any())).thenReturn(true);

        assertDoesNotThrow(() -> categoryService.deleteCategory(category.getId()));
    }

    @Test
    void givenId_whenDeleteCategory_throwCategoryNotFoundException() {
        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(getRandomLong()));
    }

    private Long getRandomLong() {
        return (long) new Random()
                .ints(0, 10)
                .findFirst()
                .getAsInt();
    }

}
