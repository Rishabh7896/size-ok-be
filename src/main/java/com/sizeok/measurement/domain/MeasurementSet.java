package com.sizeok.measurement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "measurement_sets")
public class MeasurementSet {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "profile_id", nullable = false, length = 64)
    private String profileId;

    @Column(nullable = false)
    private int version;

    @Column(name = "original_unit", nullable = false, length = 16)
    private String originalUnit;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public MeasurementSet() {
    }

    public MeasurementSet(
            String id,
            String profileId,
            int version,
            String originalUnit,
            String source,
            String status,
            OffsetDateTime measuredAt,
            String notes,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.profileId = profileId;
        this.version = version;
        this.originalUnit = originalUnit;
        this.source = source;
        this.status = status;
        this.measuredAt = measuredAt;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProfileId() {
        return profileId;
    }

    public int getVersion() {
        return version;
    }

    public String getOriginalUnit() {
        return originalUnit;
    }

    public String getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
