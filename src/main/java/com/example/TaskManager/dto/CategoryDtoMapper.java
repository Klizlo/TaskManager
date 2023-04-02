package com.example.TaskManager.dto;

import com.example.TaskManager.model.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryDtoMapper {

    public static List<CategoryDto> mapToCategoryDtos(List<Category> categories){
        return categories.stream().map(CategoryDtoMapper::mapToCategoryDto).toList();
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .owner(UserDtoMapper.mapToUserDto(category.getOwner()))
                .build();
    }

}
