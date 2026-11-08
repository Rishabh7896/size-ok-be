package com.sizeok.sizechart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class SizeChartDtos {

    private SizeChartDtos() {
    }

    public record ChartDimensionRangeDto(
            @NotBlank String dimension,
            BigDecimal minValue,
            BigDecimal maxValue,
            String measurementBasis
    ) {
    }

    public record SizeOptionDto(
            @NotBlank String sizeLabel,
            Integer sortOrder,
            @NotEmpty @Valid List<ChartDimensionRangeDto> measurements
    ) {
    }

    public record CreateSizeChartRequest(
            @NotBlank String brandId,
            String productId,
            @NotBlank String categoryId,
            String region,
            @NotBlank String chartName,
            @NotBlank String measurementBasis,
            String unit,
            String sourceType,
            String sourceUrl,
            String extractionMethod,
            BigDecimal confidence,
            Boolean markVerifiedAndActive,
            @NotEmpty @Valid List<SizeOptionDto> sizeOptions
    ) {
    }

    public record SizeChartResponse(
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
            OffsetDateTime verifiedAt,
            String verifiedBy,
            OffsetDateTime createdAt,
            List<SizeOptionDto> sizeOptions
    ) {
    }
}
