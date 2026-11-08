package com.sizeok.measurement.repository;

import com.sizeok.measurement.domain.ReferenceSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferenceSizeRepository extends JpaRepository<ReferenceSize, String> {
    List<ReferenceSize> findByProfileIdOrderByCreatedAtDesc(String profileId);
    List<ReferenceSize> findByProfileIdAndCategoryIdOrderByCreatedAtDesc(String profileId, String categoryId);
}
