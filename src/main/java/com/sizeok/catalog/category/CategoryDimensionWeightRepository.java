package com.sizeok.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDimensionWeightRepository extends JpaRepository<CategoryDimensionWeight, String> {
    List<CategoryDimensionWeight> findByCategoryId(String categoryId);
}
