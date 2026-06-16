package com.houseapp.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BuildingContactRequest(
    @NotBlank @Size(max = 120) String nameUk,
    @Size(max = 120) String nameEn,
    @NotBlank @Size(max = 120) String roleUk,
    @Size(max = 120) String roleEn,
    @Size(max = 120) String departmentUk,
    @Size(max = 120) String departmentEn,
    @Size(max = 40) @Pattern(regexp = "^$|^\\+38\\d{10}$|^\\+38\\(\\d{3}\\)-\\d{3}-\\d{2}-\\d{2}$", message = "Phone must use format +38(067)-123-45-67") String phone,
    @Email @Size(max = 255) String email,
    @Size(max = 255) String availabilityUk,
    @Size(max = 255) String availabilityEn,
    Integer sortOrder,
    Boolean active
) {}
