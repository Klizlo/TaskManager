package com.example.TaskManager.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenDto {

    private String jwt;
    private UserDto user;

}
