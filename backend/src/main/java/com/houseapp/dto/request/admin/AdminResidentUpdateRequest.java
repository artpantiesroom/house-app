package com.houseapp.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminResidentUpdateRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 255) String email,
    Long apartmentId,
    @Size(max = 40) @Pattern(regexp = "^$|^\\+38\\d{10}$|^\\+38\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$", message = "Phone must use format +38(067)-123-45-67") String phone,
    @Size(max = 120) String emergencyContactName,
    @Size(max = 40) @Pattern(regexp = "^$|^\\+38\\d{10}$|^\\+38\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$", message = "Emergency contact phone must use format +38(067)-123-45-67") String emergencyContactPhone,
    @Size(max = 255) String avatarPath,
    @Size(max = 1000) String notes,
    @NotNull Boolean enabled,
    @NotNull Boolean mustChangePassword,
    @Size(max = 10) String preferredLanguage
) {}
