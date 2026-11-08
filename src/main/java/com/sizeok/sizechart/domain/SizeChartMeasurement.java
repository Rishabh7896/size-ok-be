package com.sizeok.sizechart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "size_chart_measurements")
public class SizeChartMeasurement {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "size_option_id", nullable = false, length = 64)
    private String sizeOptionId;

    @Column(name = "dimension_code", nullable = false, length = 64)
    private String dimensionCode;

    @Column(name = "min_value_cm", nullable = false, precision = 8, scale = 2)
    private BigDecimal minValueCm;

    @Column(name = "max_value_cm", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxValueCm;

    @Column(name = "original_unit", nullable = false, length = 16)
    private String originalUnit;

    @Column(name = "measurement_basis", nullable = false, length = 32)
    private String measurementBasis;

    public SizeChartMeasurement() {
    }

    public SizeChartMeasurement(
            String id,
            String sizeOptionId,
            String dimensionCode,
            BigDecimal minValueCm,
            BigDecimal maxValueCm,
            String originalUnit,
            String measurementBasis
    ) {
        this.id = id;
        this.sizeOptionId = sizeOptionId;
        this.dimensionCode = dimensionCode;
        this.minValueCm = minValueCm;
        this.maxValueCm = maxValueCm;
        this.originalUnit = originalUnit;
        this.measurementBasis = measurementBasis;
    }

    public String getId() {
        return id;
    }

    public String getSizeOptionId() {
        return sizeOptionId;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public BigDecimal getMinValueCm() {
        return minValueCm;
    }

    public BigDecimal getMaxValueCm() {
        return maxValueCm;
    }

    public String getOriginalUnit() {
        return originalUnit;
    }

    public String getMeasurementBasis() {
        return measurementBasis;
    }
}
