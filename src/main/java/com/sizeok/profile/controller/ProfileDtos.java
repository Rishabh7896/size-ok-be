package com.sizeok.profile.controller;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record CreateProfileRequest(
            @NotBlank(message = "Profile name is required") String name,
            String relationship,
            String genderCategory,
            String preferredUnit,
            String region,
            String defaultFitPreference,
            Boolean primary
    ) {
    }

    public record UpdateProfileRequest(
            String name,
            String relationship,
            String genderCategory,
            String preferredUnit,
            String region,
            String defaultFitPreference
    ) {
    }

    public record ProfileResponse(
            String id,
            String userId,
            String name,
            String relationship,
            String genderCategory,
            String preferredUnit,
            String region,
            String defaultFitPreference,
            boolean primary,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
