package com.sizeok.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "actor_id", nullable = false, length = 64)
    private String actorId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public AuditEvent() {
    }

    public AuditEvent(String id, String actorId, String action, String entityType, String entityId, String detailsJson, OffsetDateTime createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.detailsJson = detailsJson;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
