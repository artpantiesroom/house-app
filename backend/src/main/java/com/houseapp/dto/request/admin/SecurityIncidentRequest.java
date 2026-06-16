package com.houseapp.dto.request.admin;

import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SecurityIncidentRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 160, message = "Title must be 160 characters or fewer")
    String title,

    @NotBlank(message = "Description is required")
    @Size(max = 3000, message = "Description must be 3000 characters or fewer")
    String description,

    @NotNull(message = "Severity is required")
    SecurityIncidentSeverity severity,

    SecurityIncidentStatus status,

    @NotNull(message = "Category is required")
    SecurityIncidentCategory category,

    Long assignedToUserId,
    Long relatedAuditLogId,

    @Size(max = 3000, message = "Resolution notes must be 3000 characters or fewer")
    String resolutionNotes
) {}
