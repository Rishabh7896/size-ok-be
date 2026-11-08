package com.sizeok.ingestion;

import com.sizeok.sizechart.controller.SizeChartDtos.SizeOptionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class IngestionDtos {

    private IngestionDtos() {
    }

    public record SubmitIngestionRequest(
            String profileId,
            @NotBlank String productUrl,
            String fitPreference,
            String idempotencyKey
    ) {
    }

    public record SubmitManualSizeChartRequest(
            String brandId,
            String categoryId,
            String region,
            String chartName,
            @NotBlank String measurementBasis,
            String unit,
            @NotEmpty @Valid List<SizeOptionDto> sizeOptions
    ) {
    }

    public record IngestionStatusResponse(
            String ingestionId,
            String status,
            String message,
            String requiredAction,
            Map<String, Object> product,
            Map<String, Object> recommendation,
            String extractedSizeChartId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
