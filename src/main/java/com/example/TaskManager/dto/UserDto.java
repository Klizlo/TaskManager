package com.example.TaskManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private Set<RoleDto> roles;

}
