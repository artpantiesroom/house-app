package com.houseapp.dto.response.admin;

import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceStatus;
import java.time.Instant;

public record MaintenanceRequestAdminResponse(
    Long id,
    String title,
    String description,
    MaintenanceCategory category,
    MaintenancePriority priority,
    MaintenanceStatus status,
    String adminResponse,
    String internalNotes,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt,
    Long residentId,
    String residentName,
    String residentEmail,
    Long apartmentId,
    String apartmentNumber,
    String buildingSection,
    Integer floor
) {}
