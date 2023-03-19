package com.example.TaskManager.controller;

import com.example.TaskManager.dto.RoleDto;
import com.example.TaskManager.dto.RoleDtoMapper;
import com.example.TaskManager.model.Role;
import com.example.TaskManager.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    private final IRoleService roleService;

    @GetMapping("/roles")
    public List<RoleDto> findAllRoles() {
        return RoleDtoMapper.mapToRoleDtos(roleService.findAllRoles());
    }

    @GetMapping("/roles/{roleDetail}")
    public RoleDto findRole(@PathVariable("roleDetail") String roleDetail) {
        if(NumberUtils.isCreatable(roleDetail)) {
            try {
                return RoleDtoMapper.mapToRoleDto(roleService.findRoleById(Long.parseLong(roleDetail)));
            } catch (NumberFormatException ignored) {}
        }

        return RoleDtoMapper.mapToRoleDto(roleService.findRoleByName(roleDetail));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto addRole(@RequestBody Role role){
        return RoleDtoMapper.mapToRoleDto(roleService.addRole(role));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public RoleDto updateRole(@PathVariable("id") Long id, @RequestBody Role role){
        return RoleDtoMapper.mapToRoleDto(roleService.updateRole(id, role));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteRole(@PathVariable("id") Long id){
        roleService.deleteRole(id);
    }
}
