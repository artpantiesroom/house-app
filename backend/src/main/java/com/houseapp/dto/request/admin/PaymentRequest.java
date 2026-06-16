package com.houseapp.dto.request.admin;

import com.houseapp.entity.PaymentCurrency;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PaymentRequest(
    @NotNull Long residentProfileId,
    @NotNull PaymentType type,
    PaymentStatus status,
    @NotNull @Positive Long amountMinor,
    PaymentCurrency currency,
    @NotNull @Min(2000) @Max(2100) Integer periodYear,
    @NotNull @Min(1) @Max(12) Integer periodMonth,
    @NotBlank @Size(max = 160) String titleUk,
    @Size(max = 160) String titleEn,
    @Size(max = 1000) String descriptionUk,
    @Size(max = 1000) String descriptionEn,
    @NotNull LocalDate dueDate
) {}
