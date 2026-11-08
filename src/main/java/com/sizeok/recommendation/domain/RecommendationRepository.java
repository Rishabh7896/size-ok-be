package com.sizeok.recommendation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, String> {
    List<Recommendation> findByProfileIdOrderByCreatedAtDesc(String profileId);
}
