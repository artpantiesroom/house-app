package com.houseapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houseapp.dto.response.admin.AuditLogResponse;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.AuditLog;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.AuditLogMapper;
import com.houseapp.repository.AuditLogRepository;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);

  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;
  private final AuditLogMapper auditLogMapper;
  private final ObjectMapper objectMapper;

  public AuditLogService(
      AuditLogRepository auditLogRepository,
      UserRepository userRepository,
      AuditLogMapper auditLogMapper,
      ObjectMapper objectMapper
  ) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
    this.auditLogMapper = auditLogMapper;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public List<AuditLogResponse> list(
      AuditAction action,
      AuditEntityType entityType,
      Long actorUserId,
      Long entityId,
      Instant dateFrom,
      Instant dateTo,
      String search
  ) {
    return auditLogRepository.search(action, entityType, actorUserId, entityId, dateFrom, dateTo, cleanNullable(search)).stream()
        .map(auditLogMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AuditLogResponse get(Long id) {
    return auditLogMapper.toResponse(auditLogRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Audit log not found")));
  }

  @Transactional
  public void record(
      UserPrincipal actor,
      AuditAction action,
      AuditEntityType entityType,
      Long entityId,
      String summary,
      Map<String, ?> metadata,
      HttpServletRequest request
  ) {
    try {
      User actorUser = actor == null ? null : userRepository.findById(actor.getId()).orElse(null);
      save(actorUser, actor == null ? null : actor.getUsername(), actor == null ? null : actor.getRole().name(),
          action, entityType, entityId, summary, metadata, ipAddress(request), userAgent(request));
    } catch (RuntimeException exception) {
      LOGGER.warn("Audit logging failed for action {}", action, exception);
    }
  }

  @Transactional
  public void recordUser(
      User actor,
      AuditAction action,
      AuditEntityType entityType,
      Long entityId,
      String summary,
      Map<String, ?> metadata,
      HttpServletRequest request
  ) {
    try {
      save(actor, actor == null ? null : actor.getEmail(), actor == null ? null : actor.getRole().name(),
          action, entityType, entityId, summary, metadata, ipAddress(request), userAgent(request));
    } catch (RuntimeException exception) {
      LOGGER.warn("Audit logging failed for action {}", action, exception);
    }
  }

  @Transactional
  public void recordSystem(AuditAction action, AuditEntityType entityType, Long entityId, String summary, Map<String, ?> metadata) {
    try {
      save(null, "system", "SYSTEM", action, entityType, entityId, summary, metadata, null, null);
    } catch (RuntimeException exception) {
      LOGGER.warn("System audit logging failed for action {}", action, exception);
    }
  }

  private void save(
      User actorUser,
      String actorEmail,
      String actorRole,
      AuditAction action,
      AuditEntityType entityType,
      Long entityId,
      String summary,
      Map<String, ?> metadata,
      String ipAddress,
      String userAgent
  ) {
    AuditLog auditLog = new AuditLog();
    auditLog.setActorUser(actorUser);
    auditLog.setActorEmail(cleanNullable(actorEmail));
    auditLog.setActorRole(cleanNullable(actorRole));
    auditLog.setAction(action);
    auditLog.setEntityType(entityType);
    auditLog.setEntityId(entityId);
    auditLog.setSummary(clean(summary));
    auditLog.setMetadataJson(toSafeJson(metadata));
    auditLog.setIpAddress(cleanNullable(ipAddress));
    auditLog.setUserAgent(truncate(cleanNullable(userAgent), 500));
    auditLogRepository.save(auditLog);
  }

  private String toSafeJson(Map<String, ?> metadata) {
    if (metadata == null || metadata.isEmpty()) {
      return null;
    }
    Map<String, ?> safe = metadata.entrySet().stream()
        .filter(entry -> !isSensitiveKey(entry.getKey()))
        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (safe.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(safe);
    } catch (JsonProcessingException exception) {
      return null;
    }
  }

  private boolean isSensitiveKey(String key) {
    String lower = key == null ? "" : key.toLowerCase();
    return lower.contains("password")
        || lower.contains("token")
        || lower.contains("secret")
        || lower.contains("authorization")
        || lower.contains("hash");
  }

  private String ipAddress(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String userAgent(HttpServletRequest request) {
    return request == null ? null : request.getHeader("User-Agent");
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }

  private String truncate(String value, int max) {
    return value != null && value.length() > max ? value.substring(0, max) : value;
  }
}
