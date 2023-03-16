package com.example.TaskManager.controller.credentials;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SignInCredentials {

    @NotNull(message = "Please provide username")
    private String username;
    @NotNull(message = "Please provide password")
    private String password;
}
