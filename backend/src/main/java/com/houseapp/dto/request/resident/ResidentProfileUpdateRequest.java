package com.houseapp.dto.request.resident;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResidentProfileUpdateRequest(
    @Size(max = 40) @Pattern(regexp = "^$|^\\+38\\d{10}$|^\\+38\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$", message = "Phone must use format +38(067)-123-45-67") String phone,
    @Size(max = 120) String emergencyContactName,
    @Size(max = 40) @Pattern(regexp = "^$|^\\+38\\d{10}$|^\\+38\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$", message = "Emergency contact phone must use format +38(067)-123-45-67") String emergencyContactPhone,
    @Size(max = 10) String preferredLanguage,
    String email,
    String role,
    Long apartmentId,
    String notes,
    Boolean enabled,
    Boolean mustChangePassword
) {}
