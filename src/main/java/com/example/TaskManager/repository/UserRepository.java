package com.example.TaskManager.repository;

import com.example.TaskManager.model.Task;
import com.example.TaskManager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("select u.tasks from User u where u.id = ?1")
    List<Task> findTasksByUser(Long id);
}
