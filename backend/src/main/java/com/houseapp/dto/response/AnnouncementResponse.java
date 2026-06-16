package com.houseapp.dto.response;

import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import java.time.Instant;

public record AnnouncementResponse(
    Long id,
    String titleUk,
    String titleEn,
    String bodyUk,
    String bodyEn,
    AnnouncementCategory category,
    AnnouncementPriority priority,
    AnnouncementStatus status,
    Instant publishedAt,
    Instant expiresAt,
    Long createdByUserId,
    String createdByEmail,
    Instant createdAt,
    Instant updatedAt
) {}
