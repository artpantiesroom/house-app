package com.houseapp.dto.request.admin;

import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceStatus;
import jakarta.validation.constraints.Size;

public record MaintenanceRequestUpdateRequest(
    MaintenanceStatus status,
    MaintenancePriority priority,
    @Size(max = 3000) String adminResponse,
    @Size(max = 3000) String internalNotes
) {}
