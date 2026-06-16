package com.houseapp.dto.response.admin;

import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import java.time.Instant;

public record SecurityIncidentResponse(
    Long id,
    String title,
    String description,
    SecurityIncidentSeverity severity,
    SecurityIncidentStatus status,
    SecurityIncidentCategory category,
    Long reportedByUserId,
    String reportedByEmail,
    Long assignedToUserId,
    String assignedToEmail,
    Long relatedAuditLogId,
    String resolutionNotes,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt
) {}
