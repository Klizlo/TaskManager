package com.example.TaskManager.dto;

import com.example.TaskManager.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDtoMapper {

    public static List<UserDto> mapToUserDtos(List<User> users){
        return users.stream().map(UserDtoMapper::mapToUserDto).collect(Collectors.toList());
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(new HashSet<>(RoleDtoMapper.mapToRoleDtos(user.getRoles().stream().toList())))
                .build();
    }

}
