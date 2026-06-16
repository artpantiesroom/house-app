package com.houseapp.controller;

import com.houseapp.dto.request.admin.SecurityIncidentRequest;
import com.houseapp.dto.request.admin.SecurityIncidentStatusUpdateRequest;
import com.houseapp.dto.response.admin.SecurityIncidentResponse;
import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.SecurityIncidentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/security-incidents")
public class AdminSecurityIncidentController {
  private final SecurityIncidentService securityIncidentService;

  public AdminSecurityIncidentController(SecurityIncidentService securityIncidentService) {
    this.securityIncidentService = securityIncidentService;
  }

  @GetMapping
  public List<SecurityIncidentResponse> list(
      @RequestParam(required = false) SecurityIncidentSeverity severity,
      @RequestParam(required = false) SecurityIncidentStatus status,
      @RequestParam(required = false) SecurityIncidentCategory category,
      @RequestParam(required = false) Long assignedToUserId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
      @RequestParam(required = false) String search
  ) {
    return securityIncidentService.list(severity, status, category, assignedToUserId, dateFrom, dateTo, search);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SecurityIncidentResponse create(
      @Valid @RequestBody SecurityIncidentRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return securityIncidentService.create(request, principal, servletRequest);
  }

  @GetMapping("/{id}")
  public SecurityIncidentResponse get(@PathVariable Long id) {
    return securityIncidentService.get(id);
  }

  @PutMapping("/{id}")
  public SecurityIncidentResponse update(
      @PathVariable Long id,
      @Valid @RequestBody SecurityIncidentRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return securityIncidentService.update(id, request, principal, servletRequest);
  }

  @PatchMapping("/{id}/status")
  public SecurityIncidentResponse updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody SecurityIncidentStatusUpdateRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return securityIncidentService.updateStatus(id, request, principal, servletRequest);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void softDelete(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    securityIncidentService.markFalsePositive(id, principal, servletRequest);
  }
}
