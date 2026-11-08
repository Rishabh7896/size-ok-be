package com.sizeok.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public final class FeedbackDtos {

    private FeedbackDtos() {
    }

    public record SubmitFeedbackRequest(
            @NotNull Boolean purchased,
            @NotBlank String actualSize,
            String keptSize,
            @NotBlank String fitResult,
            Boolean returnedDueToSize,
            List<String> problemDimensions,
            String userComment
    ) {
    }

    public record FeedbackResponse(
            String id,
            String recommendationId,
            Integer profileMeasurementVersion,
            int chartVersion,
            boolean purchased,
            String actualSize,
            String keptSize,
            String fitResult,
            boolean returnedDueToSize,
            List<String> problemDimensions,
            String userComment,
            OffsetDateTime createdAt
    ) {
    }
}
