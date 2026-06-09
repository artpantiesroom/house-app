package com.houseapp.dto.response;

import com.houseapp.entity.Role;
import java.time.Instant;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    boolean mustChangePassword,
    String preferredLanguage,
    Instant lastLoginAt
) {
}
