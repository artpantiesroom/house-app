package com.houseapp.dto.response.admin;

import com.houseapp.entity.ApartmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ApartmentResponse(
    Long id,
    String buildingSection,
    Integer floor,
    String apartmentNumber,
    BigDecimal areaSqM,
    Integer rooms,
    ApartmentStatus status,
    Instant createdAt,
    Instant updatedAt
) {}
