package com.houseapp.dto.request.admin;

import com.houseapp.entity.SecurityIncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SecurityIncidentStatusUpdateRequest(
    @NotNull(message = "Status is required")
    SecurityIncidentStatus status,

    @Size(max = 3000, message = "Resolution notes must be 3000 characters or fewer")
    String resolutionNotes,

    Long assignedToUserId
) {}
