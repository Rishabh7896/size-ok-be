package com.sizeok.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "fit_feedback")
public class FitFeedback {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "recommendation_id", nullable = false, length = 64)
    private String recommendationId;

    @Column(nullable = false)
    private boolean purchased;

    @Column(name = "actual_size", nullable = false, length = 32)
    private String actualSize;

    @Column(name = "kept_size", length = 32)
    private String keptSize;

    @Column(name = "fit_result", nullable = false, length = 32)
    private String fitResult;

    @Column(name = "returned_due_to_size", nullable = false)
    private boolean returnedDueToSize;

    @Column(name = "problem_dimensions", length = 255)
    private String problemDimensions;

    @Column(name = "user_comment", length = 1000)
    private String userComment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public FitFeedback() {
    }

    public FitFeedback(
            String id,
            String recommendationId,
            boolean purchased,
            String actualSize,
            String keptSize,
            String fitResult,
            boolean returnedDueToSize,
            String problemDimensions,
            String userComment,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.recommendationId = recommendationId;
        this.purchased = purchased;
        this.actualSize = actualSize;
        this.keptSize = keptSize;
        this.fitResult = fitResult;
        this.returnedDueToSize = returnedDueToSize;
        this.problemDimensions = problemDimensions;
        this.userComment = userComment;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public String getActualSize() {
        return actualSize;
    }

    public String getKeptSize() {
        return keptSize;
    }

    public String getFitResult() {
        return fitResult;
    }

    public boolean isReturnedDueToSize() {
        return returnedDueToSize;
    }

    public String getProblemDimensions() {
        return problemDimensions;
    }

    public String getUserComment() {
        return userComment;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
