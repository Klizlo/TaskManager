package com.example.TaskManager.repository;

import com.example.TaskManager.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameAndOwnerId(String name, Long owner);

}
