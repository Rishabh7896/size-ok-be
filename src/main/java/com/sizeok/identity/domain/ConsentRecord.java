package com.sizeok.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_records")
public class ConsentRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(name = "analytics_consent", nullable = false)
    private boolean analyticsConsent;

    @Column(name = "personalized_recommendations_consent", nullable = false)
    private boolean personalizedRecommendationsConsent;

    @Column(name = "image_measurement_consent", nullable = false)
    private boolean imageMeasurementConsent;

    @Column(name = "marketing_consent", nullable = false)
    private boolean marketingConsent;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public ConsentRecord() {
    }

    public ConsentRecord(String id, String userId, boolean analyticsConsent, boolean personalizedRecommendationsConsent, boolean imageMeasurementConsent, boolean marketingConsent, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.analyticsConsent = analyticsConsent;
        this.personalizedRecommendationsConsent = personalizedRecommendationsConsent;
        this.imageMeasurementConsent = imageMeasurementConsent;
        this.marketingConsent = marketingConsent;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAnalyticsConsent() {
        return analyticsConsent;
    }

    public void setAnalyticsConsent(boolean analyticsConsent) {
        this.analyticsConsent = analyticsConsent;
    }

    public boolean isPersonalizedRecommendationsConsent() {
        return personalizedRecommendationsConsent;
    }

    public void setPersonalizedRecommendationsConsent(boolean personalizedRecommendationsConsent) {
        this.personalizedRecommendationsConsent = personalizedRecommendationsConsent;
    }

    public boolean isImageMeasurementConsent() {
        return imageMeasurementConsent;
    }

    public void setImageMeasurementConsent(boolean imageMeasurementConsent) {
        this.imageMeasurementConsent = imageMeasurementConsent;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
