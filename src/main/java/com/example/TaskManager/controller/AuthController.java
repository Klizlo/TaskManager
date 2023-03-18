package com.example.TaskManager.controller;

import com.example.TaskManager.controller.credentials.SignInCredentials;
import com.example.TaskManager.controller.credentials.SignUpCredentials;
import com.example.TaskManager.dto.JwtTokenDto;
import com.example.TaskManager.dto.UserDtoMapper;
import com.example.TaskManager.model.User;
import com.example.TaskManager.security.JwtUtils;
import com.example.TaskManager.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public JwtTokenDto login(@Valid @RequestBody SignInCredentials credentials){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        User loggedUser = userService.findUserByUsername(credentials.getUsername());

        return JwtTokenDto.builder()
                .jwt(token)
                .user(UserDtoMapper.mapToUserDto(loggedUser))
                .build();
    }

    @PostMapping("/signup")
    public JwtTokenDto register(@Valid @RequestBody SignUpCredentials credentials){

        User user = new User();
        user.setUsername(credentials.getUsername());
        user.setEmail(credentials.getEmail());
        user.setPassword(credentials.getPassword());

        User loggedUser = userService.addUser(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);

        return JwtTokenDto.builder()
                .jwt(token)
                .user(UserDtoMapper.mapToUserDto(loggedUser))
                .build();
    }

}
