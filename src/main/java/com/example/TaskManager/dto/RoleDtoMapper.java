package com.example.TaskManager.dto;

import com.example.TaskManager.model.Role;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleDtoMapper {

    public static List<RoleDto> mapToRoleDtos(List<Role> roles){
        return roles.stream().map(RoleDtoMapper::mapToRoleDto).collect(Collectors.toList());
    }

    public static RoleDto mapToRoleDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

}
