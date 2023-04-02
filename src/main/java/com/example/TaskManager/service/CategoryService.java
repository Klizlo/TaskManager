package com.example.TaskManager.service;

import com.example.TaskManager.exception.CategoryAlreadyExistsException;
import com.example.TaskManager.exception.CategoryNotFoundException;
import com.example.TaskManager.exception.ForbiddenException;
import com.example.TaskManager.model.Category;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final IUserService userService;

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(!category.getOwner().getId().equals(loggedUser.getId())
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        return category;
    }

    @Override
    @Transactional
    public Category addCategory(Category category) {

        User owner = userService.findUserById(category.getOwner().getId());

        if (categoryRepository.existsByNameAndOwnerId(category.getName(), category.getOwner().getId()))
            throw new CategoryAlreadyExistsException(category.getName());

        category.setOwner(owner);

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category categoryToEdit = findCategoryById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(!loggedUser.getId().equals(categoryToEdit.getOwner().getId())
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        if (categoryRepository.existsByNameAndOwnerId(category.getName(), categoryToEdit.getOwner().getId()))
            throw new CategoryAlreadyExistsException(category.getName());

        categoryToEdit.setName(category.getName());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id))
            throw new CategoryNotFoundException(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(loggedUser.getCategories().stream().map(Category::getId).noneMatch(category -> category.equals(id))
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        categoryRepository.deleteById(id);
    }
}
