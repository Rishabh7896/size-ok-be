package com.sizeok.sizechart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "size_charts")
public class SizeChart {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "brand_id", nullable = false, length = 64)
    private String brandId;

    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(nullable = false, length = 16)
    private String region;

    @Column(name = "chart_name", nullable = false, length = 255)
    private String chartName;

    @Column(name = "measurement_basis", nullable = false, length = 32)
    private String measurementBasis;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "extraction_method", nullable = false, length = 64)
    private String extractionMethod;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal confidence;

    @Column(name = "extracted_at")
    private OffsetDateTime extractedAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "verified_by", length = 64)
    private String verifiedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public SizeChart() {
    }

    public SizeChart(
            String id,
            String brandId,
            String productId,
            String categoryId,
            String region,
            String chartName,
            String measurementBasis,
            int version,
            String status,
            String sourceType,
            String sourceUrl,
            String extractionMethod,
            BigDecimal confidence,
            OffsetDateTime extractedAt,
            OffsetDateTime verifiedAt,
            String verifiedBy,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.brandId = brandId;
        this.productId = productId;
        this.categoryId = categoryId;
        this.region = region;
        this.chartName = chartName;
        this.measurementBasis = measurementBasis;
        this.version = version;
        this.status = status;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
        this.extractionMethod = extractionMethod;
        this.confidence = confidence;
        this.extractedAt = extractedAt;
        this.verifiedAt = verifiedAt;
        this.verifiedBy = verifiedBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getProductId() {
        return productId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getRegion() {
        return region;
    }

    public String getChartName() {
        return chartName;
    }

    public String getMeasurementBasis() {
        return measurementBasis;
    }

    public int getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public OffsetDateTime getExtractedAt() {
        return extractedAt;
    }

    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isVerifiedOrActive() {
        return "ACTIVE".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status);
    }
}
