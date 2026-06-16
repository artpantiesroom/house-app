package com.houseapp.service;

import com.houseapp.dto.request.admin.SecurityIncidentRequest;
import com.houseapp.dto.request.admin.SecurityIncidentStatusUpdateRequest;
import com.houseapp.dto.response.admin.SecurityIncidentResponse;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.AuditLog;
import com.houseapp.entity.SecurityIncident;
import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.SecurityIncidentMapper;
import com.houseapp.repository.AuditLogRepository;
import com.houseapp.repository.SecurityIncidentRepository;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityIncidentService {
  private final SecurityIncidentRepository securityIncidentRepository;
  private final UserRepository userRepository;
  private final AuditLogRepository auditLogRepository;
  private final SecurityIncidentMapper securityIncidentMapper;
  private final AuditLogService auditLogService;

  public SecurityIncidentService(
      SecurityIncidentRepository securityIncidentRepository,
      UserRepository userRepository,
      AuditLogRepository auditLogRepository,
      SecurityIncidentMapper securityIncidentMapper,
      AuditLogService auditLogService
  ) {
    this.securityIncidentRepository = securityIncidentRepository;
    this.userRepository = userRepository;
    this.auditLogRepository = auditLogRepository;
    this.securityIncidentMapper = securityIncidentMapper;
    this.auditLogService = auditLogService;
  }

  @Transactional(readOnly = true)
  public List<SecurityIncidentResponse> list(
      SecurityIncidentSeverity severity,
      SecurityIncidentStatus status,
      SecurityIncidentCategory category,
      Long assignedToUserId,
      Instant dateFrom,
      Instant dateTo,
      String search
  ) {
    return securityIncidentRepository.search(severity, status, category, assignedToUserId, dateFrom, dateTo, cleanNullable(search)).stream()
        .map(securityIncidentMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public SecurityIncidentResponse get(Long id) {
    return securityIncidentMapper.toResponse(findIncident(id));
  }

  @Transactional
  public SecurityIncidentResponse create(SecurityIncidentRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    SecurityIncident incident = new SecurityIncident();
    incident.setReportedBy(findUser(principal.getId()));
    applyRequest(incident, request);
    SecurityIncident saved = securityIncidentRepository.save(incident);
    auditLogService.record(principal, AuditAction.SECURITY_INCIDENT_CREATED, AuditEntityType.SECURITY_INCIDENT, saved.getId(),
        "Security incident created: " + saved.getTitle(), Map.of("severity", saved.getSeverity(), "status", saved.getStatus()), servletRequest);
    return securityIncidentMapper.toResponse(saved);
  }

  @Transactional
  public SecurityIncidentResponse update(Long id, SecurityIncidentRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    SecurityIncident incident = findIncident(id);
    SecurityIncidentStatus previousStatus = incident.getStatus();
    applyRequest(incident, request);
    SecurityIncident saved = securityIncidentRepository.save(incident);
    AuditAction action = isResolvedStatus(saved.getStatus()) && previousStatus != saved.getStatus()
        ? AuditAction.SECURITY_INCIDENT_RESOLVED
        : AuditAction.SECURITY_INCIDENT_UPDATED;
    auditLogService.record(principal, action, AuditEntityType.SECURITY_INCIDENT, saved.getId(),
        "Security incident updated: " + saved.getTitle(), Map.of("status", saved.getStatus()), servletRequest);
    return securityIncidentMapper.toResponse(saved);
  }

  @Transactional
  public SecurityIncidentResponse updateStatus(Long id, SecurityIncidentStatusUpdateRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    SecurityIncident incident = findIncident(id);
    SecurityIncidentStatus previousStatus = incident.getStatus();
    incident.setStatus(request.status());
    if (request.assignedToUserId() != null) {
      incident.setAssignedTo(findUser(request.assignedToUserId()));
    }
    if (request.resolutionNotes() != null) {
      incident.setResolutionNotes(cleanNullable(request.resolutionNotes()));
    }
    applyResolvedAt(incident);
    SecurityIncident saved = securityIncidentRepository.save(incident);
    AuditAction action = isResolvedStatus(saved.getStatus()) && previousStatus != saved.getStatus()
        ? AuditAction.SECURITY_INCIDENT_RESOLVED
        : AuditAction.SECURITY_INCIDENT_UPDATED;
    auditLogService.record(principal, action, AuditEntityType.SECURITY_INCIDENT, saved.getId(),
        "Security incident status changed to " + saved.getStatus(), Map.of("previousStatus", previousStatus, "status", saved.getStatus()), servletRequest);
    return securityIncidentMapper.toResponse(saved);
  }

  @Transactional
  public void markFalsePositive(Long id, UserPrincipal principal, HttpServletRequest servletRequest) {
    SecurityIncident incident = findIncident(id);
    SecurityIncidentStatus previousStatus = incident.getStatus();
    incident.setStatus(SecurityIncidentStatus.FALSE_POSITIVE);
    if (incident.getResolutionNotes() == null) {
      incident.setResolutionNotes("Soft closed as false positive.");
    }
    applyResolvedAt(incident);
    securityIncidentRepository.save(incident);
    auditLogService.record(principal, AuditAction.SECURITY_INCIDENT_RESOLVED, AuditEntityType.SECURITY_INCIDENT, incident.getId(),
        "Security incident marked false positive", Map.of("previousStatus", previousStatus, "status", incident.getStatus()), servletRequest);
  }

  private void applyRequest(SecurityIncident incident, SecurityIncidentRequest request) {
    incident.setTitle(clean(request.title()));
    incident.setDescription(clean(request.description()));
    incident.setSeverity(request.severity());
    incident.setStatus(request.status() == null ? SecurityIncidentStatus.OPEN : request.status());
    incident.setCategory(request.category());
    incident.setAssignedTo(request.assignedToUserId() == null ? null : findUser(request.assignedToUserId()));
    incident.setRelatedAuditLog(request.relatedAuditLogId() == null ? null : findAuditLog(request.relatedAuditLogId()));
    incident.setResolutionNotes(cleanNullable(request.resolutionNotes()));
    applyResolvedAt(incident);
  }

  private void applyResolvedAt(SecurityIncident incident) {
    if (isResolvedStatus(incident.getStatus()) && incident.getResolvedAt() == null) {
      incident.setResolvedAt(Instant.now());
    }
    if (!isResolvedStatus(incident.getStatus())) {
      incident.setResolvedAt(null);
    }
  }

  private boolean isResolvedStatus(SecurityIncidentStatus status) {
    return status == SecurityIncidentStatus.RESOLVED || status == SecurityIncidentStatus.FALSE_POSITIVE;
  }

  private SecurityIncident findIncident(Long id) {
    return securityIncidentRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Security incident not found"));
  }

  private User findUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
  }

  private AuditLog findAuditLog(Long id) {
    return auditLogRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Audit log not found"));
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }
}
