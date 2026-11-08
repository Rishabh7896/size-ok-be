package com.sizeok.catalog.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, String> {
    Optional<Brand> findBySlugIgnoreCase(String slug);
    Optional<Brand> findByNameIgnoreCase(String name);
    List<Brand> findByNameContainingIgnoreCaseAndActiveTrueOrderByNameAsc(String query);
    List<Brand> findByActiveTrueOrderByNameAsc();
}
