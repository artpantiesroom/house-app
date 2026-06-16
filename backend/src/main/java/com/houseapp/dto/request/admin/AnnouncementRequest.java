package com.houseapp.dto.request.admin;

import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AnnouncementRequest(
    @NotBlank @Size(max = 160) String titleUk,
    @Size(max = 160) String titleEn,
    @NotBlank @Size(max = 5000) String bodyUk,
    @Size(max = 5000) String bodyEn,
    @NotNull AnnouncementCategory category,
    @NotNull AnnouncementPriority priority,
    @NotNull AnnouncementStatus status,
    Instant expiresAt
) {}
