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
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "resident_profile_id", nullable = false)
  private ResidentProfile residentProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "apartment_id")
  private Apartment apartment;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, length = 3000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MaintenanceCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MaintenancePriority priority = MaintenancePriority.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MaintenanceStatus status = MaintenanceStatus.NEW;

  @Column(name = "admin_response", length = 3000)
  private String adminResponse;

  @Column(name = "internal_notes", length = 3000)
  private String internalNotes;

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

  public Long getId() {
    return id;
  }

  public ResidentProfile getResidentProfile() {
    return residentProfile;
  }

  public void setResidentProfile(ResidentProfile residentProfile) {
    this.residentProfile = residentProfile;
  }

  public Apartment getApartment() {
    return apartment;
  }

  public void setApartment(Apartment apartment) {
    this.apartment = apartment;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public MaintenanceCategory getCategory() {
    return category;
  }

  public void setCategory(MaintenanceCategory category) {
    this.category = category;
  }

  public MaintenancePriority getPriority() {
    return priority;
  }

  public void setPriority(MaintenancePriority priority) {
    this.priority = priority;
  }

  public MaintenanceStatus getStatus() {
    return status;
  }

  public void setStatus(MaintenanceStatus status) {
    this.status = status;
  }

  public String getAdminResponse() {
    return adminResponse;
  }

  public void setAdminResponse(String adminResponse) {
    this.adminResponse = adminResponse;
  }

  public String getInternalNotes() {
    return internalNotes;
  }

  public void setInternalNotes(String internalNotes) {
    this.internalNotes = internalNotes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(Instant resolvedAt) {
    this.resolvedAt = resolvedAt;
  }
}
