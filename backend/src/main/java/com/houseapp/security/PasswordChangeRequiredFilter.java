package com.houseapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseapp.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;

  public PasswordChangeRequiredFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal
        && principal.isMustChangePassword() && !isAllowedWhilePasswordChangeRequired(request)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType("application/json");
      objectMapper.writeValue(response.getWriter(), new ErrorResponse(
          Instant.now(),
          HttpStatus.FORBIDDEN.value(),
          "PASSWORD_CHANGE_REQUIRED",
          "Password change is required before accessing this resource",
          request.getRequestURI(),
          Map.of()
      ));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isAllowedWhilePasswordChangeRequired(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/api/auth/change-password")
        || path.equals("/api/auth/logout")
        || path.equals("/api/auth/me")
        || path.equals("/api/health");
  }
}
