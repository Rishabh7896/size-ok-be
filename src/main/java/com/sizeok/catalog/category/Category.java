package com.sizeok.catalog.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "parent_id", length = 64)
    private String parentId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "garment_group", nullable = false, length = 64)
    private String garmentGroup;

    @Column(name = "gender_audience", nullable = false, length = 32)
    private String genderAudience;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Category() {
    }

    public Category(String id, String parentId, String name, String slug, String garmentGroup, String genderAudience, boolean active, OffsetDateTime createdAt) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.slug = slug;
        this.garmentGroup = garmentGroup;
        this.genderAudience = genderAudience;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getGarmentGroup() {
        return garmentGroup;
    }

    public String getGenderAudience() {
        return genderAudience;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
