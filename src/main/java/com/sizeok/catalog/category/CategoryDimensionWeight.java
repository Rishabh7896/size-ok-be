package com.sizeok.catalog.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "category_dimension_weights")
public class CategoryDimensionWeight {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(name = "dimension_code", nullable = false, length = 64)
    private String dimensionCode;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(name = "default_ease_slim_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal defaultEaseSlimCm;

    @Column(name = "default_ease_regular_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal defaultEaseRegularCm;

    @Column(name = "default_ease_relaxed_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal defaultEaseRelaxedCm;

    @Column(name = "default_ease_oversized_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal defaultEaseOversizedCm;

    @Column(name = "is_primary_dimension", nullable = false)
    private boolean primaryDimension;

    public CategoryDimensionWeight() {
    }

    public CategoryDimensionWeight(
            String id,
            String categoryId,
            String dimensionCode,
            BigDecimal weight,
            BigDecimal defaultEaseSlimCm,
            BigDecimal defaultEaseRegularCm,
            BigDecimal defaultEaseRelaxedCm,
            BigDecimal defaultEaseOversizedCm,
            boolean primaryDimension
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.dimensionCode = dimensionCode;
        this.weight = weight;
        this.defaultEaseSlimCm = defaultEaseSlimCm;
        this.defaultEaseRegularCm = defaultEaseRegularCm;
        this.defaultEaseRelaxedCm = defaultEaseRelaxedCm;
        this.defaultEaseOversizedCm = defaultEaseOversizedCm;
        this.primaryDimension = primaryDimension;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public BigDecimal getDefaultEaseSlimCm() {
        return defaultEaseSlimCm;
    }

    public BigDecimal getDefaultEaseRegularCm() {
        return defaultEaseRegularCm;
    }

    public BigDecimal getDefaultEaseRelaxedCm() {
        return defaultEaseRelaxedCm;
    }

    public BigDecimal getDefaultEaseOversizedCm() {
        return defaultEaseOversizedCm;
    }

    public boolean isPrimaryDimension() {
        return primaryDimension;
    }

    public BigDecimal getEaseForFitPreference(String fitPreference) {
        if (fitPreference == null) {
            return defaultEaseRegularCm;
        }
        return switch (fitPreference.toUpperCase()) {
            case "SLIM", "TIGHT", "FITTED" -> defaultEaseSlimCm;
            case "RELAXED", "LOOSE" -> defaultEaseRelaxedCm;
            case "OVERSIZED" -> defaultEaseOversizedCm;
            default -> defaultEaseRegularCm;
        };
    }
}
