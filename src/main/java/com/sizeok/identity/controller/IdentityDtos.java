package com.sizeok.identity.controller;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class IdentityDtos {

    private IdentityDtos() {
    }

    public record RegisterRequest(
            String email,
            String phone,
            @NotBlank(message = "password is required") String password,
            String name,
            String preferredUnit,
            String region,
            String role
    ) {
    }

    public record LoginRequest(
            String email,
            String phone,
            @NotBlank(message = "password is required") String password
    ) {
    }

    public record AuthResponse(
            String userId,
            String email,
            String phone,
            String role,
            String accessToken,
            String refreshToken,
            String defaultProfileId
    ) {
    }

    public record UserResponse(
            String id,
            String email,
            String phone,
            String authProvider,
            String role,
            String status,
            OffsetDateTime createdAt
    ) {
    }

    public record UpdateUserRequest(
            String email,
            String phone
    ) {
    }

    public record PrivacySettingsResponse(
            String userId,
            boolean analyticsConsent,
            boolean personalizedRecommendationsConsent,
            boolean imageMeasurementConsent,
            boolean marketingConsent,
            OffsetDateTime updatedAt
    ) {
    }

    public record UpdatePrivacySettingsRequest(
            Boolean analyticsConsent,
            Boolean personalizedRecommendationsConsent,
            Boolean imageMeasurementConsent,
            Boolean marketingConsent
    ) {
    }

    public record DeletionRequestResponse(
            String userId,
            String status,
            String message,
            OffsetDateTime requestedAt
    ) {
    }

    public record UserDataExportResponse(
            UserResponse user,
            PrivacySettingsResponse privacy,
            List<Map<String, Object>> profiles,
            OffsetDateTime exportedAt
    ) {
    }
}
