package com.sizeok.catalog.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandAliasRepository extends JpaRepository<BrandAlias, String> {
    List<BrandAlias> findByBrandId(String brandId);
    Optional<BrandAlias> findByAliasIgnoreCase(String alias);
}
