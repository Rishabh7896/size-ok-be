package com.sizeok.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 50)
    private String relationship;

    @Column(name = "gender_category", nullable = false, length = 50)
    private String genderCategory;

    @Column(name = "preferred_unit", nullable = false, length = 16)
    private String preferredUnit;

    @Column(nullable = false, length = 16)
    private String region;

    @Column(name = "default_fit_preference", nullable = false, length = 32)
    private String defaultFitPreference;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Profile() {
    }

    public Profile(
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
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.relationship = relationship;
        this.genderCategory = genderCategory;
        this.preferredUnit = preferredUnit;
        this.region = region;
        this.defaultFitPreference = defaultFitPreference;
        this.primary = primary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getGenderCategory() {
        return genderCategory;
    }

    public void setGenderCategory(String genderCategory) {
        this.genderCategory = genderCategory;
    }

    public String getPreferredUnit() {
        return preferredUnit;
    }

    public void setPreferredUnit(String preferredUnit) {
        this.preferredUnit = preferredUnit;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDefaultFitPreference() {
        return defaultFitPreference;
    }

    public void setDefaultFitPreference(String defaultFitPreference) {
        this.defaultFitPreference = defaultFitPreference;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
