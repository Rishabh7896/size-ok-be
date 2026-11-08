package com.sizeok.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByActiveTrueOrderByNameAsc();
    Optional<Category> findBySlugIgnoreCase(String slug);
}
