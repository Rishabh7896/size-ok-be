package com.sizeok.recommendation.controller;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class RecommendationDtos {

    private RecommendationDtos() {
    }

    public record GenerateRecommendationRequest(
            @NotBlank String profileId,
            @NotBlank String productId,
            String fitPreference
    ) {
    }

    public record GenerateRecommendationByUrlRequest(
            @NotBlank String profileId,
            @NotBlank String productUrl,
            String fitPreference
    ) {
    }

    public record DimensionFitDetail(
            String status,
            String contribution,
            double userValueCm,
            double targetMinCm,
            double targetMaxCm,
            double penalty
    ) {
    }

    public record CandidateSizeSummary(
            String sizeLabel,
            int fitScore,
            int rank
    ) {
    }

    public record RecommendationResponse(
            String recommendationId,
            String profileId,
            String productId,
            Integer measurementSetVersion,
            String sizeChartId,
            int chartVersion,
            String fitPreference,
            String recommendedSize,
            String alternativeSize,
            int fitScore,
            String confidence,
            String fitSummary,
            Map<String, DimensionFitDetail> dimensionAnalysis,
            List<String> explanation,
            List<CandidateSizeSummary> candidates,
            String algorithmVersion,
            OffsetDateTime createdAt
    ) {
    }
}
