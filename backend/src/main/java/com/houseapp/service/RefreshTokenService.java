package com.houseapp.service;

import com.houseapp.entity.RefreshToken;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final SecureRandom secureRandom = new SecureRandom();
  private final long refreshTokenDays;
  private final long rememberRefreshTokenDays;

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository,
      @Value("${app.refresh-token-days}") long refreshTokenDays,
      @Value("${app.refresh-token-remember-days}") long rememberRefreshTokenDays
  ) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenDays = refreshTokenDays;
    this.rememberRefreshTokenDays = rememberRefreshTokenDays;
  }

  @Transactional
  public IssuedRefreshToken issue(User user, boolean rememberMe) {
    String rawToken = createRawToken();
    Instant expiresAt = Instant.now().plus(rememberMe ? rememberRefreshTokenDays : refreshTokenDays, ChronoUnit.DAYS);
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(hash(rawToken));
    refreshToken.setExpiresAt(expiresAt);
    refreshToken.setRememberMe(rememberMe);
    refreshTokenRepository.save(refreshToken);
    return new IssuedRefreshToken(rawToken, expiresAt, rememberMe);
  }

  @Transactional
  public RotatedRefreshToken rotate(String rawToken) {
    RefreshToken existing = findUsable(rawToken);
    Instant now = Instant.now();
    existing.setRevokedAt(now);
    existing.setLastUsedAt(now);
    refreshTokenRepository.save(existing);
    IssuedRefreshToken next = issue(existing.getUser(), existing.isRememberMe());
    return new RotatedRefreshToken(existing.getUser(), next.rawToken(), next.expiresAt());
  }

  @Transactional
  public void revoke(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      return;
    }
    refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
      if (token.getRevokedAt() == null) {
        token.setRevokedAt(Instant.now());
        token.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(token);
      }
    });
  }

  @Transactional
  public void revokeAllForUser(Long userId) {
    refreshTokenRepository.revokeAllActiveForUser(userId, Instant.now());
  }

  private RefreshToken findUsable(String rawToken) {
    RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
        .orElseThrow(() -> invalidRefreshToken());
    if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(Instant.now()) || !token.getUser().isEnabled()) {
      throw invalidRefreshToken();
    }
    return token;
  }

  private ApiException invalidRefreshToken() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid refresh token");
  }

  private String createRawToken() {
    byte[] bytes = new byte[48];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  public record IssuedRefreshToken(String rawToken, Instant expiresAt, boolean rememberMe) {
  }

  public record RotatedRefreshToken(User user, String rawToken, Instant expiresAt) {
  }
}
