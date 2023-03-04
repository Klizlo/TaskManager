package com.example.TaskManager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    @NotNull(message = "Please provide username")
    String username;
    @Column(unique = true, nullable = false)
    @NotNull(message = "Please provide email")
    @Email
    String email;
    @Column(nullable = false)
    @NotNull(message = "Please provide password")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,}$",
            message = "Password must contain at least 1 capital letter, 1 number and 1 special character")
    @Length(min = 8, message = "Password must contain at least 8 characters")
    String password;
    @Column(name = "created_at", updatable = false)
    @CreatedDate
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    @LastModifiedDate
    LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
