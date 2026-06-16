package com.houseapp.dto.response;

import java.time.Instant;

public record BuildingContactResponse(
    Long id,
    String nameUk,
    String nameEn,
    String roleUk,
    String roleEn,
    String departmentUk,
    String departmentEn,
    String phone,
    String email,
    String availabilityUk,
    String availabilityEn,
    Integer sortOrder,
    Boolean active,
    Instant createdAt,
    Instant updatedAt
) {}
