package com.sizeok.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "profile_id", nullable = false, length = 64)
    private String profileId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "measurement_set_id", length = 64)
    private String measurementSetId;

    @Column(name = "measurement_set_version")
    private Integer measurementSetVersion;

    @Column(name = "size_chart_id", nullable = false, length = 64)
    private String sizeChartId;

    @Column(name = "chart_version", nullable = false)
    private int chartVersion;

    @Column(name = "fit_preference", nullable = false, length = 32)
    private String fitPreference;

    @Column(name = "recommended_size", length = 32)
    private String recommendedSize;

    @Column(name = "alternative_size", length = 32)
    private String alternativeSize;

    @Column(name = "fit_score", nullable = false)
    private int fitScore;

    @Column(nullable = false, length = 32)
    private String confidence;

    @Column(name = "fit_summary", nullable = false, length = 255)
    private String fitSummary;

    @Column(name = "dimension_analysis_json", nullable = false, columnDefinition = "TEXT")
    private String dimensionAnalysisJson;

    @Column(name = "explanation_json", nullable = false, columnDefinition = "TEXT")
    private String explanationJson;

    @Column(name = "snapshot_measurements_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotMeasurementsJson;

    @Column(name = "algorithm_version", nullable = false, length = 32)
    private String algorithmVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Recommendation() {
    }

    public Recommendation(
            String id,
            String profileId,
            String productId,
            String measurementSetId,
            Integer measurementSetVersion,
            String sizeChartId,
            int chartVersion,
            String fitPreference,
            String recommendedSize,
            String alternativeSize,
            int fitScore,
            String confidence,
            String fitSummary,
            String dimensionAnalysisJson,
            String explanationJson,
            String snapshotMeasurementsJson,
            String algorithmVersion,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.profileId = profileId;
        this.productId = productId;
        this.measurementSetId = measurementSetId;
        this.measurementSetVersion = measurementSetVersion;
        this.sizeChartId = sizeChartId;
        this.chartVersion = chartVersion;
        this.fitPreference = fitPreference;
        this.recommendedSize = recommendedSize;
        this.alternativeSize = alternativeSize;
        this.fitScore = fitScore;
        this.confidence = confidence;
        this.fitSummary = fitSummary;
        this.dimensionAnalysisJson = dimensionAnalysisJson;
        this.explanationJson = explanationJson;
        this.snapshotMeasurementsJson = snapshotMeasurementsJson;
        this.algorithmVersion = algorithmVersion;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getProductId() {
        return productId;
    }

    public String getMeasurementSetId() {
        return measurementSetId;
    }

    public Integer getMeasurementSetVersion() {
        return measurementSetVersion;
    }

    public String getSizeChartId() {
        return sizeChartId;
    }

    public int getChartVersion() {
        return chartVersion;
    }

    public String getFitPreference() {
        return fitPreference;
    }

    public String getRecommendedSize() {
        return recommendedSize;
    }

    public String getAlternativeSize() {
        return alternativeSize;
    }

    public int getFitScore() {
        return fitScore;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getFitSummary() {
        return fitSummary;
    }

    public String getDimensionAnalysisJson() {
        return dimensionAnalysisJson;
    }

    public String getExplanationJson() {
        return explanationJson;
    }

    public String getSnapshotMeasurementsJson() {
        return snapshotMeasurementsJson;
    }

    public String getAlgorithmVersion() {
        return algorithmVersion;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
