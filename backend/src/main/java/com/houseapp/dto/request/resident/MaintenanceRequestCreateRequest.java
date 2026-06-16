package com.houseapp.dto.request.resident;

import com.houseapp.entity.MaintenanceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaintenanceRequestCreateRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 3000) String description,
    @NotNull MaintenanceCategory category
) {}
