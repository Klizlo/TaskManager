package com.example.TaskManager.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private UserDto owner;

}
