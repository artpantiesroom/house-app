package com.houseapp.dto.response.resident;

import java.time.Instant;

public record ResidentProfileResponse(
    Long id,
    Long userId,
    String name,
    String email,
    String role,
    String preferredLanguage,
    String phone,
    Long apartmentId,
    String apartmentNumber,
    String buildingSection,
    Integer floor,
    String emergencyContactName,
    String emergencyContactPhone,
    String avatarPath,
    Instant createdAt,
    Instant updatedAt
) {}
