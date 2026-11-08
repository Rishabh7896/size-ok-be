package com.sizeok.catalog.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByCanonicalUrl(String canonicalUrl);
    List<Product> findByBrandIdOrderByCreatedAtDesc(String brandId);
    List<Product> findByCategoryIdOrderByCreatedAtDesc(String categoryId);
}
