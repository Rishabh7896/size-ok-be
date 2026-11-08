package com.sizeok.feedback;

import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.feedback.FeedbackDtos.FeedbackResponse;
import com.sizeok.feedback.FeedbackDtos.SubmitFeedbackRequest;
import com.sizeok.profile.domain.Profile;
import com.sizeok.profile.service.ProfileService;
import com.sizeok.recommendation.domain.Recommendation;
import com.sizeok.recommendation.domain.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class FeedbackService {

    private final FitFeedbackRepository fitFeedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final ProfileService profileService;
    private final AuditService auditService;

    public FeedbackService(
            FitFeedbackRepository fitFeedbackRepository,
            RecommendationRepository recommendationRepository,
            ProfileService profileService,
            AuditService auditService
    ) {
        this.fitFeedbackRepository = fitFeedbackRepository;
        this.recommendationRepository = recommendationRepository;
        this.profileService = profileService;
        this.auditService = auditService;
    }

    /**
     * Captures fit feedback linked to the exact recommendation, profile measurement version, and chart version.
     * Crucially, this does NOT alter the user's body measurements automatically.
     */
    @Transactional
    public FeedbackResponse submitFeedback(String recommendationId, SubmitFeedbackRequest req) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", recommendationId));
        Profile profile = profileService.requireAccessibleProfile(rec.getProfileId());

        String dimsCsv = (req.problemDimensions() == null || req.problemDimensions().isEmpty())
                ? ""
                : String.join(",", req.problemDimensions().stream().map(s -> s.trim().toUpperCase()).toList());

        FitFeedback feedback = new FitFeedback(
                IdGenerator.newId("fb"),
                rec.getId(),
                req.purchased(),
                req.actualSize().trim().toUpperCase(),
                req.keptSize() != null ? req.keptSize().trim().toUpperCase() : req.actualSize().trim().toUpperCase(),
                req.fitResult().trim().toUpperCase(),
                req.returnedDueToSize() != null && req.returnedDueToSize(),
                dimsCsv,
                req.userComment(),
                OffsetDateTime.now()
        );
        fitFeedbackRepository.save(feedback);
        auditService.record(profile.getUserId(), "FIT_FEEDBACK_SUBMITTED", "RECOMMENDATION", rec.getId(),
                "{\"fitResult\":\"" + feedback.getFitResult() + "\"}");
        return toResponse(feedback, rec);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackForRecommendation(String recommendationId) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", recommendationId));
        return fitFeedbackRepository.findByRecommendationIdOrderByCreatedAtDesc(recommendationId)
                .stream()
                .map(fb -> toResponse(fb, rec))
                .toList();
    }

    private FeedbackResponse toResponse(FitFeedback fb, Recommendation rec) {
        List<String> dims = (fb.getProblemDimensions() == null || fb.getProblemDimensions().isBlank())
                ? List.of()
                : Arrays.stream(fb.getProblemDimensions().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new FeedbackResponse(
                fb.getId(),
                fb.getRecommendationId(),
                rec.getMeasurementSetVersion(),
                rec.getChartVersion(),
                fb.isPurchased(),
                fb.getActualSize(),
                fb.getKeptSize(),
                fb.getFitResult(),
                fb.isReturnedDueToSize(),
                dims,
                fb.getUserComment(),
                fb.getCreatedAt()
        );
    }
}
