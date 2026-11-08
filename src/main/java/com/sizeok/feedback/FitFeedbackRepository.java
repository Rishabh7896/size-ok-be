package com.sizeok.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FitFeedbackRepository extends JpaRepository<FitFeedback, String> {
    List<FitFeedback> findByRecommendationIdOrderByCreatedAtDesc(String recommendationId);
}
