package com.houseapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "security_incidents")
public class SecurityIncident {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @NotBlank
  @Size(max = 160)
  @Column(nullable = false, length = 160)
  private String title;

  @NotBlank
  @Size(max = 3000)
  @Column(nullable = false, length = 3000)
  private String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SecurityIncidentSeverity severity;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private SecurityIncidentStatus status = SecurityIncidentStatus.OPEN;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private SecurityIncidentCategory category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reported_by_user_id")
  private User reportedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_to_user_id")
  private User assignedTo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_audit_log_id")
  private AuditLog relatedAuditLog;

  @Size(max = 3000)
  @Column(name = "resolution_notes", length = 3000)
  private String resolutionNotes;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Instant updatedAt;

  @Column(name = "resolved_at", columnDefinition = "TIMESTAMP")
  private Instant resolvedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public SecurityIncidentSeverity getSeverity() { return severity; }
  public void setSeverity(SecurityIncidentSeverity severity) { this.severity = severity; }
  public SecurityIncidentStatus getStatus() { return status; }
  public void setStatus(SecurityIncidentStatus status) { this.status = status; }
  public SecurityIncidentCategory getCategory() { return category; }
  public void setCategory(SecurityIncidentCategory category) { this.category = category; }
  public User getReportedBy() { return reportedBy; }
  public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
  public User getAssignedTo() { return assignedTo; }
  public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
  public AuditLog getRelatedAuditLog() { return relatedAuditLog; }
  public void setRelatedAuditLog(AuditLog relatedAuditLog) { this.relatedAuditLog = relatedAuditLog; }
  public String getResolutionNotes() { return resolutionNotes; }
  public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Instant getResolvedAt() { return resolvedAt; }
  public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
