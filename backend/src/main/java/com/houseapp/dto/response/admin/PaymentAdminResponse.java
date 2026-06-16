package com.houseapp.dto.response.admin;

import com.houseapp.entity.PaymentCurrency;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentAdminResponse(
    Long id,
    Long residentProfileId,
    String residentName,
    String residentEmail,
    Long apartmentId,
    String apartmentNumber,
    String buildingSection,
    Integer floor,
    PaymentType type,
    PaymentStatus status,
    Long amountMinor,
    PaymentCurrency currency,
    Integer periodYear,
    Integer periodMonth,
    String titleUk,
    String titleEn,
    String descriptionUk,
    String descriptionEn,
    LocalDate dueDate,
    Instant paidAt,
    Instant createdAt,
    Instant updatedAt
) {}
