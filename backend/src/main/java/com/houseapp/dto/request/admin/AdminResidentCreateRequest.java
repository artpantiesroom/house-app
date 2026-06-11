package com.houseapp.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResidentCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 255) String temporaryPassword,
    Long apartmentId,
    @Size(max = 40) String phone,
    @Size(max = 120) String emergencyContactName,
    @Size(max = 40) String emergencyContactPhone,
    @Size(max = 255) String avatarPath,
    @Size(max = 1000) String notes
) {}
