package com.houseapp.controller;

import com.houseapp.dto.request.ChangePasswordRequest;
import com.houseapp.dto.request.LoginRequest;
import com.houseapp.dto.request.LogoutRequest;
import com.houseapp.dto.request.RefreshRequest;
import com.houseapp.dto.response.AuthResponse;
import com.houseapp.dto.response.UserResponse;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    return authService.login(request, servletRequest);
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @Valid @RequestBody LogoutRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    authService.logout(request.refreshToken(), principal, servletRequest);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/change-password")
  public AuthResponse changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return authService.changePassword(request, principal, servletRequest);
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
    return authService.me(principal);
  }
}
