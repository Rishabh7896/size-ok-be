package com.sizeok.common.audit;

import com.sizeok.common.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(String actorId, String action, String entityType, String entityId, String detailsJson) {
        AuditEvent event = new AuditEvent(
                IdGenerator.newId("aud"),
                actorId != null ? actorId : "anonymous",
                action,
                entityType,
                entityId,
                detailsJson,
                OffsetDateTime.now()
        );
        auditEventRepository.save(event);
    }

    public List<AuditEvent> findForEntity(String entityType, String entityId) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
}
