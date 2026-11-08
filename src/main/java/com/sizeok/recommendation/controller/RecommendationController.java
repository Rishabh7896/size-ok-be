package com.sizeok.recommendation.controller;

import com.sizeok.feedback.FeedbackDtos.FeedbackResponse;
import com.sizeok.feedback.FeedbackDtos.SubmitFeedbackRequest;
import com.sizeok.feedback.FeedbackService;
import com.sizeok.ingestion.IngestionDtos.IngestionStatusResponse;
import com.sizeok.ingestion.IngestionDtos.SubmitIngestionRequest;
import com.sizeok.ingestion.IngestionService;
import com.sizeok.recommendation.controller.RecommendationDtos.GenerateRecommendationByUrlRequest;
import com.sizeok.recommendation.controller.RecommendationDtos.GenerateRecommendationRequest;
import com.sizeok.recommendation.controller.RecommendationDtos.RecommendationResponse;
import com.sizeok.recommendation.engine.RecommendationEngine;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final IngestionService ingestionService;
    private final FeedbackService feedbackService;

    public RecommendationController(
            RecommendationEngine recommendationEngine,
            IngestionService ingestionService,
            FeedbackService feedbackService
    ) {
        this.recommendationEngine = recommendationEngine;
        this.ingestionService = ingestionService;
        this.feedbackService = feedbackService;
    }

    @PostMapping("/recommendations")
    public RecommendationResponse generateRecommendation(@Valid @RequestBody GenerateRecommendationRequest request) {
        return recommendationEngine.generateRecommendation(request);
    }

    @PostMapping("/recommendations/by-url")
    public Object generateRecommendationByUrl(@Valid @RequestBody GenerateRecommendationByUrlRequest request) {
        IngestionStatusResponse ing = ingestionService.submitUrl(new SubmitIngestionRequest(
                request.profileId(), request.productUrl(), request.fitPreference(), null
        ));
        if ("COMPLETED".equals(ing.status()) && ing.product() != null && ing.product().get("productId") != null) {
            return recommendationEngine.generateRecommendation(new GenerateRecommendationRequest(
                    request.profileId(),
                    ing.product().get("productId").toString(),
                    request.fitPreference()
            ));
        }
        return ing;
    }

    @GetMapping("/recommendations/{id}")
    public RecommendationResponse getRecommendation(@PathVariable("id") String id) {
        return recommendationEngine.getRecommendation(id);
    }

    @GetMapping("/profiles/{id}/recommendations")
    public List<RecommendationResponse> getProfileRecommendations(@PathVariable("id") String profileId) {
        return recommendationEngine.getProfileRecommendations(profileId);
    }

    @PostMapping("/recommendations/{id}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submitFeedback(
            @PathVariable("id") String recommendationId,
            @Valid @RequestBody SubmitFeedbackRequest request
    ) {
        return feedbackService.submitFeedback(recommendationId, request);
    }
}
