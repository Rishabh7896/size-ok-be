package com.sizeok.identity.service;

import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.security.AuthenticatedUser;
import com.sizeok.common.security.CurrentUserProvider;
import com.sizeok.common.security.JwtTokenService;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.identity.controller.IdentityDtos.AuthProviderInfo;
import com.sizeok.identity.controller.IdentityDtos.AuthResponse;
import com.sizeok.identity.controller.IdentityDtos.DeletionRequestResponse;
import com.sizeok.identity.controller.IdentityDtos.LoginRequest;
import com.sizeok.identity.controller.IdentityDtos.OAuthLoginRequest;
import com.sizeok.identity.controller.IdentityDtos.PrivacySettingsResponse;
import com.sizeok.identity.controller.IdentityDtos.RegisterRequest;
import com.sizeok.identity.controller.IdentityDtos.UpdatePrivacySettingsRequest;
import com.sizeok.identity.controller.IdentityDtos.UpdateUserRequest;
import com.sizeok.identity.controller.IdentityDtos.UserDataExportResponse;
import com.sizeok.identity.controller.IdentityDtos.UserResponse;
import com.sizeok.identity.domain.ConsentRecord;
import com.sizeok.identity.domain.User;
import com.sizeok.identity.domain.UserSession;
import com.sizeok.identity.repository.ConsentRecordRepository;
import com.sizeok.identity.repository.UserRepository;
import com.sizeok.identity.repository.UserSessionRepository;
import com.sizeok.profile.controller.ProfileDtos.CreateProfileRequest;
import com.sizeok.profile.controller.ProfileDtos.ProfileResponse;
import com.sizeok.profile.domain.Profile;
import com.sizeok.profile.repository.ProfileRepository;
import com.sizeok.profile.service.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;

    public IdentityService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            ConsentRecordRepository consentRecordRepository,
            ProfileRepository profileRepository,
            ProfileService profileService,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            CurrentUserProvider currentUserProvider,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null;
        if (email == null) {
            throw new BadRequestException("EMAIL_REQUIRED", "Email is required");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("EMAIL_ALREADY_EXISTS", "User already registered with email: " + email);
        }

        OffsetDateTime now = OffsetDateTime.now();
        String role = request.role() != null && !request.role().isBlank() ? request.role().trim().toUpperCase() : "USER";
        User user = new User(
                IdGenerator.newId("usr"),
                email,
                passwordEncoder.encode(request.password()),
                "LOCAL",
                role,
                "ACTIVE",
                now,
                now
        );
        userRepository.save(user);
        consentRecordRepository.save(new ConsentRecord(IdGenerator.newId("cns"), user.getId(), true, true, false, false, now));

        ProfileResponse defaultProfile = profileService.createProfileForUser(
                user.getId(),
                new CreateProfileRequest(
                        request.name() != null && !request.name().isBlank() ? request.name() : "My Profile",
                        "SELF",
                        "MENS",
                        request.preferredUnit() != null ? request.preferredUnit() : "CM",
                        request.region() != null ? request.region() : "IN",
                        "REGULAR",
                        true
                )
        );

        String refreshToken = createSession(user.getId(), now);
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        auditService.record(user.getId(), "USER_REGISTERED", "USER", user.getId(), null);
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole(), accessToken, refreshToken, defaultProfile.id());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("EMAIL_REQUIRED", "Email is required to login");
        }
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadRequestException("INVALID_CREDENTIALS", "Invalid credentials"));
        if (!"ACTIVE".equals(user.getStatus()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("INVALID_CREDENTIALS", "Invalid credentials or inactive account");
        }
        OffsetDateTime now = OffsetDateTime.now();
        String refreshToken = createSession(user.getId(), now);
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        List<Profile> profiles = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(user.getId());
        String defaultProfileId = profiles.isEmpty() ? null : profiles.getFirst().getId();
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole(), accessToken, refreshToken, defaultProfileId);
    }

    @Transactional
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        String provider = request.provider() != null ? request.provider().trim().toUpperCase() : "";
        if (!List.of("GOOGLE", "APPLE", "GITHUB").contains(provider)) {
            throw new BadRequestException("UNSUPPORTED_AUTH_PROVIDER", "Unsupported OAuth provider: " + provider);
        }

        String email = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : extractClaimFromJwt(request.idToken(), "email");
        String displayName = request.name() != null && !request.name().isBlank()
                ? request.name().trim()
                : extractClaimFromJwt(request.idToken(), "name");

        if (email == null || email.isBlank()) {
            throw new BadRequestException("EMAIL_REQUIRED", "Email is required for " + provider + " authentication");
        }

        OffsetDateTime now = OffsetDateTime.now();
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        String defaultProfileId;

        if (user == null) {
            user = new User(
                    IdGenerator.newId("usr"),
                    email,
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    provider,
                    "USER",
                    "ACTIVE",
                    now,
                    now
            );
            userRepository.save(user);
            consentRecordRepository.save(new ConsentRecord(IdGenerator.newId("cns"), user.getId(), true, true, false, false, now));

            String profileName = (displayName != null && !displayName.isBlank())
                    ? displayName
                    : email.split("@")[0];
            ProfileResponse defaultProfile = profileService.createProfileForUser(
                    user.getId(),
                    new CreateProfileRequest(
                            profileName,
                            "SELF",
                            "MENS",
                            request.preferredUnit() != null && !request.preferredUnit().isBlank() ? request.preferredUnit() : "CM",
                            request.region() != null && !request.region().isBlank() ? request.region() : "IN",
                            "REGULAR",
                            true
                    )
            );
            defaultProfileId = defaultProfile.id();
            auditService.record(user.getId(), "USER_OAUTH_REGISTERED", "USER", user.getId(), "{\"provider\":\"" + provider + "\"}");
        } else {
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new BadRequestException("ACCOUNT_INACTIVE", "Account is not active");
            }
            List<Profile> profiles = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(user.getId());
            if (profiles.isEmpty()) {
                String profileName = (displayName != null && !displayName.isBlank())
                        ? displayName
                        : email.split("@")[0];
                ProfileResponse created = profileService.createProfileForUser(
                        user.getId(),
                        new CreateProfileRequest(profileName, "SELF", "MENS", "CM", "IN", "REGULAR", true)
                );
                defaultProfileId = created.id();
            } else {
                defaultProfileId = profiles.getFirst().getId();
            }
            auditService.record(user.getId(), "USER_OAUTH_LOGIN", "USER", user.getId(), "{\"provider\":\"" + provider + "\"}");
        }

        String refreshToken = createSession(user.getId(), now);
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole(), accessToken, refreshToken, defaultProfileId);
    }

    @Transactional
    public void logout() {
        currentUserProvider.getCurrentUserOptional().ifPresent(current -> {
            for (UserSession s : userSessionRepository.findByUserId(current.userId())) {
                if (!s.isRevoked()) {
                    s.setRevoked(true);
                    userSessionRepository.save(s);
                }
            }
            auditService.record(current.userId(), "USER_LOGGED_OUT", "USER", current.userId(), null);
        });
    }

    public List<AuthProviderInfo> getSupportedProviders() {
        return List.of(
                new AuthProviderInfo("GOOGLE", "Google", true),
                new AuthProviderInfo("APPLE", "Apple", true),
                new AuthProviderInfo("GITHUB", "GitHub", true),
                new AuthProviderInfo("LOCAL", "Email", true)
        );
    }

    private String extractClaimFromJwt(String token, String claim) {
        if (token == null || token.isBlank()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payloadJson);
            return node.hasNonNull(claim) ? node.get(claim).asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getMe() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        User user = userRepository.findById(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", current.userId()));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateMe(UpdateUserRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        User user = userRepository.findById(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", current.userId()));
        if (request.email() != null && !request.email().isBlank()) user.setEmail(request.email().trim().toLowerCase());
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional
    public DeletionRequestResponse initiateDeletion() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        User user = userRepository.findById(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", current.userId()));
        OffsetDateTime now = OffsetDateTime.now();
        user.setStatus("DELETION_PENDING");
        user.setEmail("deleted_" + user.getId() + "@anonymized.sizeok.local");
        user.setUpdatedAt(now);
        userRepository.save(user);
        for (UserSession s : userSessionRepository.findByUserId(user.getId())) {
            s.setRevoked(true);
            userSessionRepository.save(s);
        }
        auditService.record(user.getId(), "ACCOUNT_DELETION_INITIATED", "USER", user.getId(), null);
        return new DeletionRequestResponse(user.getId(), "DELETION_PENDING", "Account deletion workflow initiated", now);
    }

    @Transactional(readOnly = true)
    public PrivacySettingsResponse getPrivacySettings() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        ConsentRecord r = consentRecordRepository.findByUserId(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("ConsentRecord", current.userId()));
        return toPrivacyResponse(r);
    }

    @Transactional
    public PrivacySettingsResponse updatePrivacySettings(UpdatePrivacySettingsRequest req) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        ConsentRecord r = consentRecordRepository.findByUserId(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("ConsentRecord", current.userId()));
        if (req.analyticsConsent() != null) r.setAnalyticsConsent(req.analyticsConsent());
        if (req.personalizedRecommendationsConsent() != null) r.setPersonalizedRecommendationsConsent(req.personalizedRecommendationsConsent());
        if (req.imageMeasurementConsent() != null) r.setImageMeasurementConsent(req.imageMeasurementConsent());
        if (req.marketingConsent() != null) r.setMarketingConsent(req.marketingConsent());
        r.setUpdatedAt(OffsetDateTime.now());
        consentRecordRepository.save(r);
        auditService.record(current.userId(), "PRIVACY_CONSENT_UPDATED", "USER", current.userId(), null);
        return toPrivacyResponse(r);
    }

    @Transactional(readOnly = true)
    public UserDataExportResponse exportUserData() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        List<Map<String, Object>> profiles = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(current.userId())
                .stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName(), "relationship", p.getRelationship(), "preferredUnit", p.getPreferredUnit(), "region", p.getRegion()))
                .toList();
        return new UserDataExportResponse(getMe(), getPrivacySettings(), profiles, OffsetDateTime.now());
    }

    private String createSession(String userId, OffsetDateTime now) {
        String rawRefreshToken = UUID.randomUUID().toString();
        userSessionRepository.save(new UserSession(IdGenerator.newId("ses"), userId, passwordEncoder.encode(rawRefreshToken), now.plusDays(30), false, now));
        return rawRefreshToken;
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getAuthProvider(), u.getRole(), u.getStatus(), u.getCreatedAt());
    }

    private PrivacySettingsResponse toPrivacyResponse(ConsentRecord c) {
        return new PrivacySettingsResponse(c.getUserId(), c.isAnalyticsConsent(), c.isPersonalizedRecommendationsConsent(), c.isImageMeasurementConsent(), c.isMarketingConsent(), c.getUpdatedAt());
    }
}


