package com.houseapp.dto.response.admin;

import java.time.Instant;

public record AdminResidentResponse(
    Long id,
    Long userId,
    String name,
    String email,
    String phone,
    Long apartmentId,
    String apartmentNumber,
    String buildingSection,
    Integer floor,
    Boolean enabled,
    Boolean mustChangePassword,
    String preferredLanguage,
    String emergencyContactName,
    String emergencyContactPhone,
    String avatarPath,
    String avatarUrl,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
