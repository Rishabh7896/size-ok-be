package com.sizeok.profile.service;

import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.AccessDeniedOperationException;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.security.AuthenticatedUser;
import com.sizeok.common.security.CurrentUserProvider;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.common.util.UnitConverter;
import com.sizeok.ingestion.ProductIngestion;
import com.sizeok.ingestion.ProductIngestionRepository;
import com.sizeok.profile.controller.ProfileDtos.CreateProfileRequest;
import com.sizeok.profile.controller.ProfileDtos.ProfileResponse;
import com.sizeok.profile.controller.ProfileDtos.UpdateProfileRequest;
import com.sizeok.profile.domain.Profile;
import com.sizeok.profile.repository.ProfileRepository;
import com.sizeok.recommendation.domain.Recommendation;
import com.sizeok.recommendation.domain.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final RecommendationRepository recommendationRepository;
    private final ProductIngestionRepository productIngestionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public ProfileService(
            ProfileRepository profileRepository,
            RecommendationRepository recommendationRepository,
            ProductIngestionRepository productIngestionRepository,
            CurrentUserProvider currentUserProvider,
            AuditService auditService
    ) {
        this.profileRepository = profileRepository;
        this.recommendationRepository = recommendationRepository;
        this.productIngestionRepository = productIngestionRepository;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        return createProfileForUser(user.userId(), request);
    }

    @Transactional
    public ProfileResponse createProfileForUser(String userId, CreateProfileRequest request) {
        List<Profile> existing = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(userId);
        boolean isPrimary = request.primary() != null ? request.primary() : existing.isEmpty();
        OffsetDateTime now = OffsetDateTime.now();

        Profile profile = new Profile(
                IdGenerator.newId("prof"),
                userId,
                request.name().trim(),
                request.relationship() != null && !request.relationship().isBlank() ? request.relationship().trim().toUpperCase() : "SELF",
                request.genderCategory() != null && !request.genderCategory().isBlank() ? request.genderCategory().trim().toUpperCase() : "MENS",
                UnitConverter.normalizeUnit(request.preferredUnit()),
                request.region() != null && !request.region().isBlank() ? request.region().trim().toUpperCase() : "IN",
                request.defaultFitPreference() != null && !request.defaultFitPreference().isBlank() ? request.defaultFitPreference().trim().toUpperCase() : "REGULAR",
                isPrimary,
                now,
                now
        );
        profileRepository.save(profile);
        auditService.record(userId, "PROFILE_CREATED", "PROFILE", profile.getId(), null);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> listMyProfiles() {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        return profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(user.userId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Profile requireAccessibleProfile(String profileId) {
        AuthenticatedUser user = currentUserProvider.requireCurrentUser();
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId));
        if (!profile.getUserId().equals(user.userId()) && !user.isAdmin()) {
            throw new AccessDeniedOperationException("You do not have permission to access profile: " + profileId);
        }
        return profile;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String profileId) {
        return toResponse(requireAccessibleProfile(profileId));
    }

    @Transactional
    public ProfileResponse updateProfile(String profileId, UpdateProfileRequest request) {
        Profile profile = requireAccessibleProfile(profileId);
        if (request.name() != null && !request.name().isBlank()) {
            profile.setName(request.name().trim());
        }
        if (request.relationship() != null && !request.relationship().isBlank()) {
            profile.setRelationship(request.relationship().trim().toUpperCase());
        }
        if (request.genderCategory() != null && !request.genderCategory().isBlank()) {
            profile.setGenderCategory(request.genderCategory().trim().toUpperCase());
        }
        if (request.preferredUnit() != null && !request.preferredUnit().isBlank()) {
            profile.setPreferredUnit(UnitConverter.normalizeUnit(request.preferredUnit()));
        }
        if (request.region() != null && !request.region().isBlank()) {
            profile.setRegion(request.region().trim().toUpperCase());
        }
        if (request.defaultFitPreference() != null && !request.defaultFitPreference().isBlank()) {
            profile.setDefaultFitPreference(request.defaultFitPreference().trim().toUpperCase());
        }
        profile.setUpdatedAt(OffsetDateTime.now());
        profileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public void deleteProfile(String profileId) {
        Profile profile = requireAccessibleProfile(profileId);
        List<Profile> userProfiles = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(profile.getUserId());
        if (userProfiles.size() <= 1) {
            throw new BadRequestException("LAST_PROFILE_CANNOT_BE_DELETED", "Keep at least one profile in your account");
        }
        List<ProductIngestion> ingestions = productIngestionRepository.findByProfileId(profileId);
        if (!ingestions.isEmpty()) {
            productIngestionRepository.deleteAll(ingestions);
        }
        List<Recommendation> recs = recommendationRepository.findByProfileIdOrderByCreatedAtDesc(profileId);
        if (!recs.isEmpty()) {
            recommendationRepository.deleteAll(recs);
        }
        profileRepository.delete(profile);
        auditService.record(profile.getUserId(), "PROFILE_DELETED", "PROFILE", profileId, null);
    }

    public ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(),
                p.getUserId(),
                p.getName(),
                p.getRelationship(),
                p.getGenderCategory(),
                p.getPreferredUnit(),
                p.getRegion(),
                p.getDefaultFitPreference(),
                p.isPrimary(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
