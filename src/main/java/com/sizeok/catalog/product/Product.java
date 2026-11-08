package com.sizeok.catalog.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "brand_id", nullable = false, length = 64)
    private String brandId;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(nullable = false, length = 120)
    private String retailer;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "canonical_url", unique = true, length = 1000)
    private String canonicalUrl;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "fit_type", nullable = false, length = 32)
    private String fitType;

    @Column(name = "stretch_level", nullable = false, length = 32)
    private String stretchLevel;

    @Column(name = "material_composition", length = 255)
    private String materialComposition;

    @Column(name = "gender_audience", nullable = false, length = 32)
    private String genderAudience;

    @Column(nullable = false, length = 16)
    private String region;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Product() {
    }

    public Product(
            String id,
            String brandId,
            String categoryId,
            String retailer,
            String name,
            String canonicalUrl,
            String productType,
            String fitType,
            String stretchLevel,
            String materialComposition,
            String genderAudience,
            String region,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.retailer = retailer;
        this.name = name;
        this.canonicalUrl = canonicalUrl;
        this.productType = productType;
        this.fitType = fitType;
        this.stretchLevel = stretchLevel;
        this.materialComposition = materialComposition;
        this.genderAudience = genderAudience;
        this.region = region;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getRetailer() {
        return retailer;
    }

    public String getName() {
        return name;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public String getProductType() {
        return productType;
    }

    public String getFitType() {
        return fitType;
    }

    public String getStretchLevel() {
        return stretchLevel;
    }

    public String getMaterialComposition() {
        return materialComposition;
    }

    public String getGenderAudience() {
        return genderAudience;
    }

    public String getRegion() {
        return region;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
