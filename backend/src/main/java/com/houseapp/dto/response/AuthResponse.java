package com.houseapp.dto.response;

import java.time.Instant;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long accessTokenExpiresIn,
    Instant refreshTokenExpiresAt,
    UserResponse user
) {
}
