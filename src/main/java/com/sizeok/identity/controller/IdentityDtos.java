package com.sizeok.identity.controller;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class IdentityDtos {

    private IdentityDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "email is required") String email,
            @NotBlank(message = "password is required") String password,
            String name,
            String preferredUnit,
            String region,
            String role
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "email is required") String email,
            @NotBlank(message = "password is required") String password
    ) {
    }

    public record OAuthLoginRequest(
            @NotBlank(message = "provider is required") String provider,
            String idToken,
            String email,
            String name,
            String preferredUnit,
            String region
    ) {
    }

    public record AuthProviderInfo(
            String id,
            String name,
            boolean enabled
    ) {
    }

    public record AuthResponse(
            String userId,
            String email,
            String role,
            String accessToken,
            String refreshToken,
            String defaultProfileId
    ) {
    }

    public record UserResponse(
            String id,
            String email,
            String authProvider,
            String role,
            String status,
            OffsetDateTime createdAt
    ) {
    }

    public record UpdateUserRequest(
            String email
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
