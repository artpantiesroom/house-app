package com.houseapp.mapper;

import com.houseapp.dto.response.admin.SecurityIncidentResponse;
import com.houseapp.entity.SecurityIncident;
import org.springframework.stereotype.Component;

@Component
public class SecurityIncidentMapper {
  public SecurityIncidentResponse toResponse(SecurityIncident incident) {
    return new SecurityIncidentResponse(
        incident.getId(),
        incident.getTitle(),
        incident.getDescription(),
        incident.getSeverity(),
        incident.getStatus(),
        incident.getCategory(),
        incident.getReportedBy() == null ? null : incident.getReportedBy().getId(),
        incident.getReportedBy() == null ? null : incident.getReportedBy().getEmail(),
        incident.getAssignedTo() == null ? null : incident.getAssignedTo().getId(),
        incident.getAssignedTo() == null ? null : incident.getAssignedTo().getEmail(),
        incident.getRelatedAuditLog() == null ? null : incident.getRelatedAuditLog().getId(),
        incident.getResolutionNotes(),
        incident.getCreatedAt(),
        incident.getUpdatedAt(),
        incident.getResolvedAt()
    );
  }
}
