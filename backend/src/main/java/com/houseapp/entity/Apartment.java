package com.houseapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "apartments")
public class Apartment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(name = "building_section", nullable = false, length = 50)
  private String buildingSection;

  @Column(nullable = false)
  private Integer floor;

  @Column(name = "apartment_number", nullable = false, unique = true, length = 30)
  private String apartmentNumber;

  @Column(name = "area_sq_m", nullable = false, precision = 8, scale = 2)
  private BigDecimal areaSqM;

  @Column(nullable = false)
  private Integer rooms;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ApartmentStatus status = ApartmentStatus.VACANT;

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

  public String getBuildingSection() {
    return buildingSection;
  }

  public void setBuildingSection(String buildingSection) {
    this.buildingSection = buildingSection;
  }

  public Integer getFloor() {
    return floor;
  }

  public void setFloor(Integer floor) {
    this.floor = floor;
  }

  public String getApartmentNumber() {
    return apartmentNumber;
  }

  public void setApartmentNumber(String apartmentNumber) {
    this.apartmentNumber = apartmentNumber;
  }

  public BigDecimal getAreaSqM() {
    return areaSqM;
  }

  public void setAreaSqM(BigDecimal areaSqM) {
    this.areaSqM = areaSqM;
  }

  public Integer getRooms() {
    return rooms;
  }

  public void setRooms(Integer rooms) {
    this.rooms = rooms;
  }

  public ApartmentStatus getStatus() {
    return status;
  }

  public void setStatus(ApartmentStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
