package com.sizeok.measurement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reference_sizes")
public class ReferenceSize {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "profile_id", nullable = false, length = 64)
    private String profileId;

    @Column(name = "brand_id", nullable = false, length = 64)
    private String brandId;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "size_chart_id", length = 64)
    private String sizeChartId;

    @Column(name = "size_label", nullable = false, length = 32)
    private String sizeLabel;

    @Column(nullable = false, length = 16)
    private String region;

    @Column(name = "fit_preference", nullable = false, length = 32)
    private String fitPreference;

    @Column(name = "inferred_ranges_json", columnDefinition = "TEXT")
    private String inferredRangesJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ReferenceSize() {
    }

    public ReferenceSize(
            String id,
            String profileId,
            String brandId,
            String categoryId,
            String productId,
            String sizeChartId,
            String sizeLabel,
            String region,
            String fitPreference,
            String inferredRangesJson,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.profileId = profileId;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.productId = productId;
        this.sizeChartId = sizeChartId;
        this.sizeLabel = sizeLabel;
        this.region = region;
        this.fitPreference = fitPreference;
        this.inferredRangesJson = inferredRangesJson;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getProductId() {
        return productId;
    }

    public String getSizeChartId() {
        return sizeChartId;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public String getRegion() {
        return region;
    }

    public String getFitPreference() {
        return fitPreference;
    }

    public String getInferredRangesJson() {
        return inferredRangesJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
