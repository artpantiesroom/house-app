package com.houseapp.dto.request.admin;

import com.houseapp.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateRequest(
    @NotNull PaymentStatus status
) {}
