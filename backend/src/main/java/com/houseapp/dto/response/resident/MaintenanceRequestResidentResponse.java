package com.houseapp.dto.response.resident;

import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceStatus;
import java.time.Instant;

public record MaintenanceRequestResidentResponse(
    Long id,
    String title,
    String description,
    MaintenanceCategory category,
    MaintenancePriority priority,
    MaintenanceStatus status,
    String adminResponse,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt,
    String apartmentNumber
) {}
