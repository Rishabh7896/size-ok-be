package com.sizeok.catalog.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(nullable = false, length = 64)
    private String country;

    @Column(name = "applicable_size_systems", nullable = false, length = 255)
    private String applicableSizeSystems;

    @Column(name = "official_chart_url", length = 500)
    private String officialChartUrl;

    @Column(name = "verification_status", nullable = false, length = 32)
    private String verificationStatus;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Brand() {
    }

    public Brand(
            String id,
            String name,
            String slug,
            String country,
            String applicableSizeSystems,
            String officialChartUrl,
            String verificationStatus,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.country = country;
        this.applicableSizeSystems = applicableSizeSystems;
        this.officialChartUrl = officialChartUrl;
        this.verificationStatus = verificationStatus;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getApplicableSizeSystems() {
        return applicableSizeSystems;
    }

    public void setApplicableSizeSystems(String applicableSizeSystems) {
        this.applicableSizeSystems = applicableSizeSystems;
    }

    public String getOfficialChartUrl() {
        return officialChartUrl;
    }

    public void setOfficialChartUrl(String officialChartUrl) {
        this.officialChartUrl = officialChartUrl;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
