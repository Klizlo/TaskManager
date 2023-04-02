package com.example.TaskManager.service;

import com.example.TaskManager.model.Category;

import java.util.List;

public interface ICategoryService {

    List<Category> findAllCategories();
    Category findCategoryById(Long id);
    Category addCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);

}
