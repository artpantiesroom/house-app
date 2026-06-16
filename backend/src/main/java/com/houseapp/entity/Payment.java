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
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
public class Payment {
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "amount_minor", nullable = false)
  private Long amountMinor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private PaymentCurrency currency = PaymentCurrency.UAH;

  @Column(name = "period_year", nullable = false)
  private Integer periodYear;

  @Column(name = "period_month", nullable = false)
  private Integer periodMonth;

  @Column(name = "title_uk", nullable = false, length = 160)
  private String titleUk;

  @Column(name = "title_en", length = 160)
  private String titleEn;

  @Column(name = "description_uk", length = 1000)
  private String descriptionUk;

  @Column(name = "description_en", length = 1000)
  private String descriptionEn;

  @Column(name = "due_date", nullable = false, columnDefinition = "DATE")
  private LocalDate dueDate;

  @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
  private Instant paidAt;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

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

  public PaymentType getType() {
    return type;
  }

  public void setType(PaymentType type) {
    this.type = type;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public Long getAmountMinor() {
    return amountMinor;
  }

  public void setAmountMinor(Long amountMinor) {
    this.amountMinor = amountMinor;
  }

  public PaymentCurrency getCurrency() {
    return currency;
  }

  public void setCurrency(PaymentCurrency currency) {
    this.currency = currency;
  }

  public Integer getPeriodYear() {
    return periodYear;
  }

  public void setPeriodYear(Integer periodYear) {
    this.periodYear = periodYear;
  }

  public Integer getPeriodMonth() {
    return periodMonth;
  }

  public void setPeriodMonth(Integer periodMonth) {
    this.periodMonth = periodMonth;
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

  public String getDescriptionUk() {
    return descriptionUk;
  }

  public void setDescriptionUk(String descriptionUk) {
    this.descriptionUk = descriptionUk;
  }

  public String getDescriptionEn() {
    return descriptionEn;
  }

  public void setDescriptionEn(String descriptionEn) {
    this.descriptionEn = descriptionEn;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(Instant paidAt) {
    this.paidAt = paidAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }
}
