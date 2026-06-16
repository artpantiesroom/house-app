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
@Table(name = "announcements")
public class Announcement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(name = "title_uk", nullable = false, length = 160)
  private String titleUk;

  @Column(name = "title_en", length = 160)
  private String titleEn;

  @Column(name = "body_uk", nullable = false, length = 5000)
  private String bodyUk;

  @Column(name = "body_en", length = 5000)
  private String bodyEn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AnnouncementCategory category = AnnouncementCategory.GENERAL;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AnnouncementStatus status = AnnouncementStatus.DRAFT;

  @Column(name = "published_at", columnDefinition = "TIMESTAMP")
  private Instant publishedAt;

  @Column(name = "expires_at", columnDefinition = "TIMESTAMP")
  private Instant expiresAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by_user_id", nullable = false)
  private User createdBy;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Instant updatedAt;

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

  public String getTitleUk() {
    return titleUk;
  }

  public void setTitleUk(String titleUk) {
    this.titleUk = titleUk;
  }

  public String getTitleEn() {
    return titleEn;
  }

  public void setTitleEn(String titleEn) {
    this.titleEn = titleEn;
  }

  public String getBodyUk() {
    return bodyUk;
  }

  public void setBodyUk(String bodyUk) {
    this.bodyUk = bodyUk;
  }

  public String getBodyEn() {
    return bodyEn;
  }

  public void setBodyEn(String bodyEn) {
    this.bodyEn = bodyEn;
  }

  public AnnouncementCategory getCategory() {
    return category;
  }

  public void setCategory(AnnouncementCategory category) {
    this.category = category;
  }

  public AnnouncementPriority getPriority() {
    return priority;
  }

  public void setPriority(AnnouncementPriority priority) {
    this.priority = priority;
  }

  public AnnouncementStatus getStatus() {
    return status;
  }

  public void setStatus(AnnouncementStatus status) {
    this.status = status;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
