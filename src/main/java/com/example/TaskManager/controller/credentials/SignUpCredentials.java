package com.example.TaskManager.controller.credentials;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class SignUpCredentials {

    @NotNull(message = "Please provide email")
    @Email
    private String email;
    @NotNull(message = "Please provide username")
    private String username;
    @NotNull(message = "Please provide password")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$",
            message = "Password must contain at least 1 capital letter, 1 number and 1 special character")
    @Length(min = 8, message = "Password must contain at least 8 characters")
    private String password;

}
