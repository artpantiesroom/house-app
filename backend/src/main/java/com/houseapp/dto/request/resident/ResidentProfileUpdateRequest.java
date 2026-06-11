package com.houseapp.dto.request.resident;

import jakarta.validation.constraints.Size;

public record ResidentProfileUpdateRequest(
    @Size(max = 40) String phone,
    @Size(max = 120) String emergencyContactName,
    @Size(max = 40) String emergencyContactPhone,
    @Size(max = 10) String preferredLanguage,
    String email,
    String role,
    Long apartmentId,
    String notes,
    Boolean enabled,
    Boolean mustChangePassword
) {}
