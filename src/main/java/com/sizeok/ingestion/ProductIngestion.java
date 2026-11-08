package com.sizeok.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "product_ingestions")
public class ProductIngestion {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "profile_id", length = 64)
    private String profileId;

    @Column(name = "submitted_url", nullable = false, length = 1000)
    private String submittedUrl;

    @Column(length = 120)
    private String retailer;

    @Column(name = "fit_preference", nullable = false, length = 32)
    private String fitPreference;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "required_action", length = 64)
    private String requiredAction;

    @Column(name = "parser_version", nullable = false, length = 32)
    private String parserVersion;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "extracted_size_chart_id", length = 64)
    private String extractedSizeChartId;

    @Column(name = "recommendation_id", length = 64)
    private String recommendationId;

    @Column(name = "idempotency_key", unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "raw_metadata_json", columnDefinition = "TEXT")
    private String rawMetadataJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ProductIngestion() {
    }

    public ProductIngestion(
            String id,
            String profileId,
            String submittedUrl,
            String retailer,
            String fitPreference,
            String status,
            String requiredAction,
            String parserVersion,
            String failureReason,
            String productId,
            String extractedSizeChartId,
            String recommendationId,
            String idempotencyKey,
            String rawMetadataJson,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.profileId = profileId;
        this.submittedUrl = submittedUrl;
        this.retailer = retailer;
        this.fitPreference = fitPreference;
        this.status = status;
        this.requiredAction = requiredAction;
        this.parserVersion = parserVersion;
        this.failureReason = failureReason;
        this.productId = productId;
        this.extractedSizeChartId = extractedSizeChartId;
        this.recommendationId = recommendationId;
        this.idempotencyKey = idempotencyKey;
        this.rawMetadataJson = rawMetadataJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getSubmittedUrl() {
        return submittedUrl;
    }

    public String getRetailer() {
        return retailer;
    }

    public String getFitPreference() {
        return fitPreference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequiredAction() {
        return requiredAction;
    }

    public void setRequiredAction(String requiredAction) {
        this.requiredAction = requiredAction;
    }

    public String getParserVersion() {
        return parserVersion;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getExtractedSizeChartId() {
        return extractedSizeChartId;
    }

    public void setExtractedSizeChartId(String extractedSizeChartId) {
        this.extractedSizeChartId = extractedSizeChartId;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(String recommendationId) {
        this.recommendationId = recommendationId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRawMetadataJson() {
        return rawMetadataJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
