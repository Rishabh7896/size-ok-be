package com.sizeok.identity.service;

import com.sizeok.common.audit.AuditService;
import com.sizeok.common.exception.BadRequestException;
import com.sizeok.common.exception.ResourceNotFoundException;
import com.sizeok.common.security.AuthenticatedUser;
import com.sizeok.common.security.CurrentUserProvider;
import com.sizeok.common.security.JwtTokenService;
import com.sizeok.common.util.IdGenerator;
import com.sizeok.identity.controller.IdentityDtos.AuthResponse;
import com.sizeok.identity.controller.IdentityDtos.DeletionRequestResponse;
import com.sizeok.identity.controller.IdentityDtos.LoginRequest;
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
        String phone = request.phone() != null && !request.phone().isBlank() ? request.phone().trim() : null;
        if (email == null && phone == null) {
            throw new BadRequestException("EMAIL_OR_PHONE_REQUIRED", "Either email or phone number is required");
        }
        if (email != null && userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("EMAIL_ALREADY_EXISTS", "User already registered with email: " + email);
        }
        if (phone != null && userRepository.findByPhone(phone).isPresent()) {
            throw new BadRequestException("PHONE_ALREADY_EXISTS", "User already registered with phone: " + phone);
        }

        OffsetDateTime now = OffsetDateTime.now();
        String role = request.role() != null && !request.role().isBlank() ? request.role().trim().toUpperCase() : "USER";
        User user = new User(
                IdGenerator.newId("usr"),
                email,
                phone,
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
        return new AuthResponse(user.getId(), user.getEmail(), user.getPhone(), user.getRole(), accessToken, refreshToken, defaultProfile.id());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user;
        if (request.email() != null && !request.email().isBlank()) {
            user = userRepository.findByEmailIgnoreCase(request.email().trim())
                    .orElseThrow(() -> new BadRequestException("INVALID_CREDENTIALS", "Invalid credentials"));
        } else if (request.phone() != null && !request.phone().isBlank()) {
            user = userRepository.findByPhone(request.phone().trim())
                    .orElseThrow(() -> new BadRequestException("INVALID_CREDENTIALS", "Invalid credentials"));
        } else {
            throw new BadRequestException("EMAIL_OR_PHONE_REQUIRED", "Email or phone is required to login");
        }
        if (!"ACTIVE".equals(user.getStatus()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("INVALID_CREDENTIALS", "Invalid credentials or inactive account");
        }
        OffsetDateTime now = OffsetDateTime.now();
        String refreshToken = createSession(user.getId(), now);
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        List<Profile> profiles = profileRepository.findByUserIdOrderByPrimaryDescCreatedAtAsc(user.getId());
        String defaultProfileId = profiles.isEmpty() ? null : profiles.getFirst().getId();
        return new AuthResponse(user.getId(), user.getEmail(), user.getPhone(), user.getRole(), accessToken, refreshToken, defaultProfileId);
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
        if (request.phone() != null && !request.phone().isBlank()) user.setPhone(request.phone().trim());
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
        user.setPhone(null);
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
        return new UserResponse(u.getId(), u.getEmail(), u.getPhone(), u.getAuthProvider(), u.getRole(), u.getStatus(), u.getCreatedAt());
    }

    private PrivacySettingsResponse toPrivacyResponse(ConsentRecord c) {
        return new PrivacySettingsResponse(c.getUserId(), c.isAnalyticsConsent(), c.isPersonalizedRecommendationsConsent(), c.isImageMeasurementConsent(), c.isMarketingConsent(), c.getUpdatedAt());
    }
}


