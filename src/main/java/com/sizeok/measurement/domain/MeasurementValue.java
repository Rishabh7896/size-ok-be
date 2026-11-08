package com.sizeok.measurement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "measurement_values")
public class MeasurementValue {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "measurement_set_id", nullable = false, length = 64)
    private String measurementSetId;

    @Column(name = "dimension_code", nullable = false, length = 64)
    private String dimensionCode;

    @Column(name = "value_cm", nullable = false, precision = 8, scale = 2)
    private BigDecimal valueCm;

    @Column(name = "original_value", nullable = false, precision = 8, scale = 2)
    private BigDecimal originalValue;

    @Column(name = "original_unit", nullable = false, length = 16)
    private String originalUnit;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal confidence;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public MeasurementValue() {
    }

    public MeasurementValue(
            String id,
            String measurementSetId,
            String dimensionCode,
            BigDecimal valueCm,
            BigDecimal originalValue,
            String originalUnit,
            String source,
            BigDecimal confidence,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.measurementSetId = measurementSetId;
        this.dimensionCode = dimensionCode;
        this.valueCm = valueCm;
        this.originalValue = originalValue;
        this.originalUnit = originalUnit;
        this.source = source;
        this.confidence = confidence;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getMeasurementSetId() {
        return measurementSetId;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public BigDecimal getValueCm() {
        return valueCm;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public String getOriginalUnit() {
        return originalUnit;
    }

    public String getSource() {
        return source;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
