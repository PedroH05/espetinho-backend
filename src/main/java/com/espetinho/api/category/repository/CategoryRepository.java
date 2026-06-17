package com.espetinho.api.category.repository;

import com.espetinho.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    Optional<Category> findByIdAndActiveTrue(UUID id);
}
