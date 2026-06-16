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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_user_id")
  private User actorUser;

  @Column(name = "actor_email")
  private String actorEmail;

  @Column(name = "actor_role", length = 30)
  private String actorRole;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 60)
  private AuditAction action;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 60)
  private AuditEntityType entityType;

  @Column(name = "entity_id")
  @JdbcTypeCode(SqlTypes.INTEGER)
  private Long entityId;

  @NotBlank
  @Size(max = 500)
  @Column(nullable = false, length = 500)
  private String summary;

  @Column(name = "metadata_json", columnDefinition = "TEXT")
  private String metadataJson;

  @Size(max = 80)
  @Column(name = "ip_address", length = 80)
  private String ipAddress;

  @Size(max = 500)
  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public Long getId() { return id; }
  public User getActorUser() { return actorUser; }
  public void setActorUser(User actorUser) { this.actorUser = actorUser; }
  public String getActorEmail() { return actorEmail; }
  public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }
  public String getActorRole() { return actorRole; }
  public void setActorRole(String actorRole) { this.actorRole = actorRole; }
  public AuditAction getAction() { return action; }
  public void setAction(AuditAction action) { this.action = action; }
  public AuditEntityType getEntityType() { return entityType; }
  public void setEntityType(AuditEntityType entityType) { this.entityType = entityType; }
  public Long getEntityId() { return entityId; }
  public void setEntityId(Long entityId) { this.entityId = entityId; }
  public String getSummary() { return summary; }
  public void setSummary(String summary) { this.summary = summary; }
  public String getMetadataJson() { return metadataJson; }
  public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
  public String getIpAddress() { return ipAddress; }
  public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
  public String getUserAgent() { return userAgent; }
  public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
