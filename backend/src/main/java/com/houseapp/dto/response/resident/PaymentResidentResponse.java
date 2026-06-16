package com.houseapp.dto.response.resident;

import com.houseapp.entity.PaymentCurrency;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentResidentResponse(
    Long id,
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
    Instant updatedAt,
    String apartmentNumber
) {}
