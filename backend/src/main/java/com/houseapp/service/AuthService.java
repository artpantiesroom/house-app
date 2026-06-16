package com.houseapp.service;

import com.houseapp.dto.request.ChangePasswordRequest;
import com.houseapp.dto.request.LoginRequest;
import com.houseapp.dto.request.RefreshRequest;
import com.houseapp.dto.response.AuthResponse;
import com.houseapp.dto.response.UserResponse;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.UserMapper;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.JwtService;
import com.houseapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicyService passwordPolicyService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserMapper userMapper;
  private final AuditLogService auditLogService;

  public AuthService(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PasswordPolicyService passwordPolicyService,
      JwtService jwtService,
      RefreshTokenService refreshTokenService,
      UserMapper userMapper,
      AuditLogService auditLogService
  ) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicyService = passwordPolicyService;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.userMapper = userMapper;
    this.auditLogService = auditLogService;
  }

  public AuthResponse login(LoginRequest request, HttpServletRequest servletRequest) {
    String email = normalizeEmail(request.email());
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
    } catch (RuntimeException exception) {
      auditLogService.recordSystem(AuditAction.LOGIN_FAILED, AuditEntityType.AUTH, null, "Login failed for " + email, Map.of("email", email));
      throw new BadCredentialsException("Invalid credentials");
    }
    User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!user.isEnabled()) {
      auditLogService.recordUser(user, AuditAction.LOGIN_FAILED, AuditEntityType.AUTH, user.getId(), "Login failed for disabled user " + email, Map.of("email", email), servletRequest);
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials");
    }
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    auditLogService.recordUser(user, AuditAction.LOGIN_SUCCESS, AuditEntityType.AUTH, user.getId(), "Login successful for " + email, Map.of("rememberMe", request.rememberMe()), servletRequest);
    return issueAuthResponse(user, request.rememberMe());
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(request.refreshToken());
    return createAuthResponse(rotated.user(), rotated.rawToken(), rotated.expiresAt());
  }

  @Transactional
  public void logout(String refreshToken, UserPrincipal principal, HttpServletRequest servletRequest) {
    refreshTokenService.revoke(refreshToken);
    auditLogService.record(principal, AuditAction.LOGOUT, AuditEntityType.AUTH, principal == null ? null : principal.getId(), "Logout completed", Map.of(), servletRequest);
  }

  @Transactional
  public AuthResponse changePassword(ChangePasswordRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    passwordPolicyService.validate(request.newPassword());
    User user = userRepository.findById(principal.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required"));
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Current password is incorrect");
    }
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    user.setMustChangePassword(false);
    userRepository.save(user);
    refreshTokenService.revokeAllForUser(user.getId());
    auditLogService.recordUser(user, AuditAction.PASSWORD_CHANGED, AuditEntityType.USER, user.getId(), "Password changed", Map.of(), servletRequest);
    return issueAuthResponse(user, false);
  }

  @Transactional(readOnly = true)
  public UserResponse me(UserPrincipal principal) {
    User user = userRepository.findById(principal.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required"));
    return userMapper.toResponse(user);
  }

  private AuthResponse issueAuthResponse(User user, boolean rememberMe) {
    RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user, rememberMe);
    return createAuthResponse(user, refreshToken.rawToken(), refreshToken.expiresAt());
  }

  private AuthResponse createAuthResponse(User user, String rawRefreshToken, Instant refreshExpiresAt) {
    return new AuthResponse(
        jwtService.createAccessToken(user),
        rawRefreshToken,
        jwtService.getAccessTokenSeconds(),
        refreshExpiresAt,
        userMapper.toResponse(user)
    );
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }
}
