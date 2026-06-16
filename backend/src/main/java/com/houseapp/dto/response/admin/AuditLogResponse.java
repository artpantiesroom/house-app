package com.houseapp.dto.response.admin;

import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import java.time.Instant;

public record AuditLogResponse(
    Long id,
    Long actorUserId,
    String actorEmail,
    String actorRole,
    AuditAction action,
    AuditEntityType entityType,
    Long entityId,
    String summary,
    String metadataJson,
    String ipAddress,
    String userAgent,
    Instant createdAt
) {}
