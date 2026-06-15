package com.houseapp.service;

import com.houseapp.dto.request.ChangePasswordRequest;
import com.houseapp.dto.request.LoginRequest;
import com.houseapp.dto.request.RefreshRequest;
import com.houseapp.dto.response.AuthResponse;
import com.houseapp.dto.response.UserResponse;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.UserMapper;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.JwtService;
import com.houseapp.security.UserPrincipal;
import java.time.Instant;
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

  public AuthService(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PasswordPolicyService passwordPolicyService,
      JwtService jwtService,
      RefreshTokenService refreshTokenService,
      UserMapper userMapper
  ) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicyService = passwordPolicyService;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.userMapper = userMapper;
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
    } catch (RuntimeException exception) {
      throw new BadCredentialsException("Invalid credentials");
    }
    User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!user.isEnabled()) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials");
    }
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    return issueAuthResponse(user, request.rememberMe());
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(request.refreshToken());
    return createAuthResponse(rotated.user(), rotated.rawToken(), rotated.expiresAt());
  }

  @Transactional
  public void logout(String refreshToken) {
    refreshTokenService.revoke(refreshToken);
  }

  @Transactional
  public AuthResponse changePassword(ChangePasswordRequest request, UserPrincipal principal) {
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
