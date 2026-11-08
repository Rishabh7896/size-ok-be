package com.sizeok.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_candidates")
public class RecommendationCandidate {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "recommendation_id", nullable = false, length = 64)
    private String recommendationId;

    @Column(name = "size_label", nullable = false, length = 32)
    private String sizeLabel;

    @Column(name = "fit_score", nullable = false)
    private int fitScore;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder;

    @Column(name = "dimension_breakdown_json", columnDefinition = "TEXT")
    private String dimensionBreakdownJson;

    public RecommendationCandidate() {
    }

    public RecommendationCandidate(String id, String recommendationId, String sizeLabel, int fitScore, int rankOrder, String dimensionBreakdownJson) {
        this.id = id;
        this.recommendationId = recommendationId;
        this.sizeLabel = sizeLabel;
        this.fitScore = fitScore;
        this.rankOrder = rankOrder;
        this.dimensionBreakdownJson = dimensionBreakdownJson;
    }

    public String getId() {
        return id;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public int getFitScore() {
        return fitScore;
    }

    public int getRankOrder() {
        return rankOrder;
    }

    public String getDimensionBreakdownJson() {
        return dimensionBreakdownJson;
    }
}
