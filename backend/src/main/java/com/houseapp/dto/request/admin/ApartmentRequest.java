package com.houseapp.dto.request.admin;

import com.houseapp.entity.ApartmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ApartmentRequest(
    @NotBlank @Size(max = 50) String buildingSection,
    @NotNull @Min(0) @Max(200) Integer floor,
    @NotBlank @Size(max = 30) String apartmentNumber,
    @NotNull @DecimalMin(value = "0.01") BigDecimal areaSqM,
    @NotNull @Min(1) @Max(20) Integer rooms,
    @NotNull ApartmentStatus status
) {}
