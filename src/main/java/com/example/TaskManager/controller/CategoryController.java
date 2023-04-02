package com.example.TaskManager.controller;

import com.example.TaskManager.dto.CategoryDto;
import com.example.TaskManager.dto.CategoryDtoMapper;
import com.example.TaskManager.model.Category;
import com.example.TaskManager.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<CategoryDto> findAllCategories(){
        return CategoryDtoMapper.mapToCategoryDtos(categoryService.findAllCategories());
    }

    @GetMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CategoryDto findCategory(@PathVariable("id") Long id){
        return CategoryDtoMapper.mapToCategoryDto(categoryService.findCategoryById(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody Category category){
        return CategoryDtoMapper.mapToCategoryDto(categoryService.addCategory(category));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public CategoryDto updateCategory(@PathVariable("id") Long id, @Valid @RequestBody Category category) {
        return CategoryDtoMapper.mapToCategoryDto(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
    }
}
