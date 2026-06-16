package com.houseapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "building_contacts")
public class BuildingContact {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JdbcTypeCode(SqlTypes.INTEGER)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(name = "name_uk", nullable = false, length = 120)
  private String nameUk;

  @Column(name = "name_en", length = 120)
  private String nameEn;

  @Column(name = "role_uk", nullable = false, length = 120)
  private String roleUk;

  @Column(name = "role_en", length = 120)
  private String roleEn;

  @Column(name = "department_uk", length = 120)
  private String departmentUk;

  @Column(name = "department_en", length = 120)
  private String departmentEn;

  @Column(length = 40)
  private String phone;

  @Column(length = 255)
  private String email;

  @Column(name = "availability_uk", length = 255)
  private String availabilityUk;

  @Column(name = "availability_en", length = 255)
  private String availabilityEn;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @Column(nullable = false, columnDefinition = "BOOLEAN")
  private boolean active = true;

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

  public String getNameUk() {
    return nameUk;
  }

  public void setNameUk(String nameUk) {
    this.nameUk = nameUk;
  }

  public String getNameEn() {
    return nameEn;
  }

  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }

  public String getRoleUk() {
    return roleUk;
  }

  public void setRoleUk(String roleUk) {
    this.roleUk = roleUk;
  }

  public String getRoleEn() {
    return roleEn;
  }

  public void setRoleEn(String roleEn) {
    this.roleEn = roleEn;
  }

  public String getDepartmentUk() {
    return departmentUk;
  }

  public void setDepartmentUk(String departmentUk) {
    this.departmentUk = departmentUk;
  }

  public String getDepartmentEn() {
    return departmentEn;
  }

  public void setDepartmentEn(String departmentEn) {
    this.departmentEn = departmentEn;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAvailabilityUk() {
    return availabilityUk;
  }

  public void setAvailabilityUk(String availabilityUk) {
    this.availabilityUk = availabilityUk;
  }

  public String getAvailabilityEn() {
    return availabilityEn;
  }

  public void setAvailabilityEn(String availabilityEn) {
    this.availabilityEn = availabilityEn;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
