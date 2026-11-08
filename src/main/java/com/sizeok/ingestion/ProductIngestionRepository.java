package com.sizeok.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductIngestionRepository extends JpaRepository<ProductIngestion, String> {
    Optional<ProductIngestion> findByIdempotencyKey(String idempotencyKey);
    List<ProductIngestion> findByProfileId(String profileId);
}
