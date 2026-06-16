package com.houseapp.mapper;

import com.houseapp.dto.response.admin.AuditLogResponse;
import com.houseapp.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
  public AuditLogResponse toResponse(AuditLog auditLog) {
    return new AuditLogResponse(
        auditLog.getId(),
        auditLog.getActorUser() == null ? null : auditLog.getActorUser().getId(),
        auditLog.getActorEmail(),
        auditLog.getActorRole(),
        auditLog.getAction(),
        auditLog.getEntityType(),
        auditLog.getEntityId(),
        auditLog.getSummary(),
        auditLog.getMetadataJson(),
        auditLog.getIpAddress(),
        auditLog.getUserAgent(),
        auditLog.getCreatedAt()
    );
  }
}
