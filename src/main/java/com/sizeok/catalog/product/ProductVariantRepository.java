package com.sizeok.catalog.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductIdOrderBySortOrderAsc(String productId);
}
