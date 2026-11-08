package com.sizeok.measurement.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class MeasurementDtos {

    private MeasurementDtos() {
    }

    public record MeasurementDimensionInput(
            @NotBlank String dimension,
            @NotNull BigDecimal value,
            String source,
            BigDecimal confidence
    ) {
    }

    public record CreateMeasurementSetRequest(
            String unit,
            String source,
            String notes,
            @NotEmpty @Valid List<MeasurementDimensionInput> measurements
    ) {
    }

    public record PatchMeasurementSetRequest(
            String unit,
            String source,
            String notes,
            @NotEmpty @Valid List<MeasurementDimensionInput> measurements
    ) {
    }

    public record MeasurementValueDto(
            String dimension,
            BigDecimal valueCm,
            BigDecimal originalValue,
            String originalUnit,
            String source,
            BigDecimal confidence
    ) {
    }

    public record MeasurementSetResponse(
            String measurementSetId,
            String profileId,
            int version,
            String unit,
            String source,
            String status,
            OffsetDateTime measuredAt,
            OffsetDateTime createdAt,
            Map<String, BigDecimal> measurementsCm,
            List<MeasurementValueDto> detailedMeasurements
    ) {
    }

    public record InferredDimensionRange(
            BigDecimal minCm,
            BigDecimal maxCm,
            String basis
    ) {
    }

    public record AddReferenceSizeRequest(
            @NotBlank String brandId,
            @NotBlank String categoryId,
            @NotBlank String sizeLabel,
            String region,
            String fitPreference,
            String productId
    ) {
    }

    public record ReferenceSizeResponse(
            String id,
            String profileId,
            String brandId,
            String categoryId,
            String productId,
            String sizeChartId,
            String sizeLabel,
            String region,
            String fitPreference,
            Map<String, InferredDimensionRange> inferredRanges,
            boolean conflictDetected,
            List<String> conflictWarnings,
            OffsetDateTime createdAt
    ) {
    }
}
