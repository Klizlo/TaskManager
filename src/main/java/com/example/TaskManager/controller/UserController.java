package com.example.TaskManager.controller;

import com.example.TaskManager.dto.TaskDto;
import com.example.TaskManager.dto.TaskDtoMapper;
import com.example.TaskManager.dto.UserDto;
import com.example.TaskManager.dto.UserDtoMapper;
import com.example.TaskManager.exception.ForbiddenException;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.model.User;
import com.example.TaskManager.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final IUserService userService;

    @GetMapping("/users")
    public List<UserDto> findAllUsers() {
        return UserDtoMapper.mapToUserDtos(userService.findAllUsers());
    }

    @GetMapping("/users/{userDetail}")
    public UserDto findUser(@PathVariable("userDetail") String userDetail) {
        if (NumberUtils.isCreatable(userDetail)) {
            try {
                return UserDtoMapper.mapToUserDto(userService.findUserById(Long.parseLong(userDetail)));
            } catch (NumberFormatException ignored){}
        }

        if (EmailValidator.getInstance().isValid(userDetail))
            return UserDtoMapper.mapToUserDto(userService.findUserByEmail(userDetail));

        return UserDtoMapper.mapToUserDto(userService.findUserByUsername(userDetail));
    }

    @GetMapping("/users/{id}/tasks")
    @PreAuthorize("hasAuthority('USER')")
    public List<TaskDto> findTaskByUser(@PathVariable("id") Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if(!loggedUser.getId().equals(id)
                && loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN")))
            throw new ForbiddenException();

        return TaskDtoMapper.mapToTaskDtos(userService.findTasksByUser(id));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody User user) {

        User addedUser = userService.addUser(user);

        if (user.getRoles().stream().map(Role::getName).anyMatch(role -> !role.equals("USER")))
            addedUser = userService.modifyUserRoles(addedUser.getId(),
                    user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));

        return UserDtoMapper.mapToUserDto(addedUser);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public UserDto modifyUser(@PathVariable("id") Long id, @RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if (loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN"))
                && !loggedUser.getId().equals(id))
            throw new ForbiddenException();

        User updatedUser = userService.updateUser(id, user);

        if(loggedUser.getRoles().stream().map(Role::getName).anyMatch(role -> role.equals("ADMIN"))
               && user.getRoles().stream().map(Role::getName).anyMatch(role -> !role.contains("USER"))){
            updatedUser = userService.modifyUserRoles(id,
                    user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
        }

        return UserDtoMapper.mapToUserDto(updatedUser);
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto modifyUserRoles(@PathVariable("id") Long userId, @RequestBody List<Long> roleIds) {
        return UserDtoMapper.mapToUserDto(userService.modifyUserRoles(userId, roleIds));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public void deleteUser(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User loggedUser = userService.findUserByUsername(userDetails.getUsername());

        if (loggedUser.getRoles().stream().map(Role::getName).noneMatch(role -> role.equals("ADMIN"))
                && !loggedUser.getId().equals(id))
            throw new ForbiddenException();

        userService.deleteUser(id);
    }

}
