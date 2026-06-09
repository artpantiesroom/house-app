package com.houseapp.security;

import com.houseapp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey secretKey;
  private final long accessTokenSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.access-token-seconds}") long accessTokenSeconds
  ) {
    if (jwtSecret.contains("dev-only-change-me")) {
      System.err.println("WARNING: using development fallback APP_JWT_SECRET. Set a strong APP_JWT_SECRET outside development.");
    }
    if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("APP_JWT_SECRET must be at least 32 bytes");
    }
    this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenSeconds = accessTokenSeconds;
  }

  public String createAccessToken(User user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(accessTokenSeconds);
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .claim("mustChangePassword", user.isMustChangePassword())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(secretKey)
        .compact();
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isValid(String token, UserPrincipal principal) {
    String subject = extractSubject(token);
    return subject.equals(principal.getUsername()) && parseClaims(token).getExpiration().after(new Date());
  }

  public long getAccessTokenSeconds() {
    return accessTokenSeconds;
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
