package com.houseapp.service;

import com.houseapp.dto.request.admin.AdminResidentCreateRequest;
import com.houseapp.dto.request.admin.AdminResidentUpdateRequest;
import com.houseapp.dto.request.resident.ResidentProfileUpdateRequest;
import com.houseapp.dto.response.admin.AdminResidentResponse;
import com.houseapp.dto.response.resident.ResidentProfileResponse;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.Apartment;
import com.houseapp.entity.ApartmentStatus;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.Role;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.ResidentProfileMapper;
import com.houseapp.repository.ApartmentRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResidentProfileService {
  private final ResidentProfileRepository residentProfileRepository;
  private final ApartmentRepository apartmentRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicyService passwordPolicyService;
  private final ResidentProfileMapper residentProfileMapper;
  private final AuditLogService auditLogService;
  private final AvatarStorageService avatarStorageService;

  public ResidentProfileService(
      ResidentProfileRepository residentProfileRepository,
      ApartmentRepository apartmentRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      PasswordPolicyService passwordPolicyService,
      ResidentProfileMapper residentProfileMapper,
      AuditLogService auditLogService,
      AvatarStorageService avatarStorageService
  ) {
    this.residentProfileRepository = residentProfileRepository;
    this.apartmentRepository = apartmentRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicyService = passwordPolicyService;
    this.residentProfileMapper = residentProfileMapper;
    this.auditLogService = auditLogService;
    this.avatarStorageService = avatarStorageService;
  }

  @Transactional(readOnly = true)
  public List<AdminResidentResponse> listForAdmin() {
    return residentProfileRepository.findAllByUserRoleOrderByCreatedAtDesc(Role.RESIDENT).stream()
        .map(residentProfileMapper::toAdminResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminResidentResponse getForAdmin(Long id) {
    return residentProfileMapper.toAdminResponse(findProfile(id));
  }

  @Transactional
  public AdminResidentResponse create(AdminResidentCreateRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    passwordPolicyService.validate(request.temporaryPassword());
    String email = normalizeEmail(request.email());
    if (userRepository.existsByEmail(email)) {
      throw conflict("Email is already used");
    }

    User user = new User();
    user.setName(clean(request.name()));
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
    user.setRole(Role.RESIDENT);
    user.setPreferredLanguage("uk");
    user.setMustChangePassword(true);
    user.setEnabled(true);

    ResidentProfile profile = new ResidentProfile();
    profile.setUser(user);
    profile.setApartment(resolveAssignableApartment(request.apartmentId(), null));
    profile.setPhone(cleanNullable(request.phone()));
    profile.setEmergencyContactName(cleanNullable(request.emergencyContactName()));
    profile.setEmergencyContactPhone(cleanNullable(request.emergencyContactPhone()));
    profile.setAvatarPath(cleanNullable(request.avatarPath()));
    profile.setNotes(cleanNullable(request.notes()));

    userRepository.save(user);
    ResidentProfile saved = residentProfileRepository.save(profile);
    updateApartmentStatuses();
    auditLogService.record(principal, AuditAction.RESIDENT_CREATED, AuditEntityType.RESIDENT, saved.getId(),
        "Resident created: " + user.getEmail(), Map.of("residentUserId", user.getId()), servletRequest);
    return residentProfileMapper.toAdminResponse(saved);
  }

  @Transactional
  public AdminResidentResponse update(Long id, AdminResidentUpdateRequest request, UserPrincipal principal, HttpServletRequest servletRequest) {
    ResidentProfile profile = findProfile(id);
    User user = profile.getUser();
    String email = normalizeEmail(request.email());
    userRepository.findByEmail(email)
        .filter(existing -> !existing.getId().equals(user.getId()))
        .ifPresent(existing -> {
          throw conflict("Email is already used");
        });

    user.setName(clean(request.name()));
    user.setEmail(email);
    user.setEnabled(Boolean.TRUE.equals(request.enabled()));
    user.setMustChangePassword(Boolean.TRUE.equals(request.mustChangePassword()));
    user.setPreferredLanguage(normalizeLanguage(request.preferredLanguage()));
    profile.setApartment(resolveAssignableApartment(request.apartmentId(), profile.getId()));
    profile.setPhone(cleanNullable(request.phone()));
    profile.setEmergencyContactName(cleanNullable(request.emergencyContactName()));
    profile.setEmergencyContactPhone(cleanNullable(request.emergencyContactPhone()));
    profile.setAvatarPath(cleanNullable(request.avatarPath()));
    profile.setNotes(cleanNullable(request.notes()));
    ResidentProfile saved = residentProfileRepository.save(profile);
    updateApartmentStatuses();
    auditLogService.record(principal, AuditAction.RESIDENT_UPDATED, AuditEntityType.RESIDENT, saved.getId(),
        "Resident updated: " + user.getEmail(), Map.of("residentUserId", user.getId(), "enabled", user.isEnabled()), servletRequest);
    return residentProfileMapper.toAdminResponse(saved);
  }

  @Transactional
  public void deactivate(Long id, UserPrincipal principal, HttpServletRequest servletRequest) {
    ResidentProfile profile = findProfile(id);
    profile.getUser().setEnabled(false);
    profile.setApartment(null);
    residentProfileRepository.save(profile);
    updateApartmentStatuses();
    auditLogService.record(principal, AuditAction.RESIDENT_DEACTIVATED, AuditEntityType.RESIDENT, profile.getId(),
        "Resident deactivated: " + profile.getUser().getEmail(), Map.of("residentUserId", profile.getUser().getId()), servletRequest);
  }

  @Transactional(readOnly = true)
  public ResidentProfileResponse getOwnProfile(UserPrincipal principal) {
    return residentProfileMapper.toResidentResponse(findByUser(principal.getId()));
  }

  @Transactional
  public ResidentProfileResponse updateOwnProfile(UserPrincipal principal, ResidentProfileUpdateRequest request) {
    rejectRestrictedResidentFields(request);
    ResidentProfile profile = findByUser(principal.getId());
    profile.setPhone(cleanNullable(request.phone()));
    profile.setEmergencyContactName(cleanNullable(request.emergencyContactName()));
    profile.setEmergencyContactPhone(cleanNullable(request.emergencyContactPhone()));
    profile.getUser().setPreferredLanguage(normalizeLanguage(request.preferredLanguage()));
    return residentProfileMapper.toResidentResponse(residentProfileRepository.save(profile));
  }

  @Transactional
  public ResidentProfileResponse uploadOwnAvatar(UserPrincipal principal, MultipartFile file, HttpServletRequest servletRequest) {
    ResidentProfile profile = findByUser(principal.getId());
    String oldAvatar = profile.getAvatarPath();
    String filename = avatarStorageService.save(file);
    profile.setAvatarPath(filename);
    ResidentProfile saved = residentProfileRepository.save(profile);
    avatarStorageService.delete(oldAvatar);
    auditLogService.record(principal, AuditAction.AVATAR_UPLOADED, AuditEntityType.RESIDENT, saved.getId(),
        "Resident avatar uploaded", Map.of("residentProfileId", saved.getId(), "filename", filename), servletRequest);
    return residentProfileMapper.toResidentResponse(saved);
  }

  @Transactional
  public ResidentProfileResponse deleteOwnAvatar(UserPrincipal principal, HttpServletRequest servletRequest) {
    ResidentProfile profile = findByUser(principal.getId());
    String oldAvatar = profile.getAvatarPath();
    profile.setAvatarPath(null);
    ResidentProfile saved = residentProfileRepository.save(profile);
    avatarStorageService.delete(oldAvatar);
    auditLogService.record(principal, AuditAction.AVATAR_DELETED, AuditEntityType.RESIDENT, saved.getId(),
        "Resident avatar deleted", Map.of("residentProfileId", saved.getId()), servletRequest);
    return residentProfileMapper.toResidentResponse(saved);
  }

  @Transactional
  public AdminResidentResponse uploadAvatarForAdmin(Long id, MultipartFile file, UserPrincipal principal, HttpServletRequest servletRequest) {
    ResidentProfile profile = findProfile(id);
    String oldAvatar = profile.getAvatarPath();
    String filename = avatarStorageService.save(file);
    profile.setAvatarPath(filename);
    ResidentProfile saved = residentProfileRepository.save(profile);
    avatarStorageService.delete(oldAvatar);
    auditLogService.record(principal, AuditAction.AVATAR_UPLOADED, AuditEntityType.RESIDENT, saved.getId(),
        "Admin uploaded resident avatar: " + saved.getUser().getEmail(), Map.of("residentProfileId", saved.getId(), "residentUserId", saved.getUser().getId(), "filename", filename), servletRequest);
    return residentProfileMapper.toAdminResponse(saved);
  }

  @Transactional
  public AdminResidentResponse deleteAvatarForAdmin(Long id, UserPrincipal principal, HttpServletRequest servletRequest) {
    ResidentProfile profile = findProfile(id);
    String oldAvatar = profile.getAvatarPath();
    profile.setAvatarPath(null);
    ResidentProfile saved = residentProfileRepository.save(profile);
    avatarStorageService.delete(oldAvatar);
    auditLogService.record(principal, AuditAction.AVATAR_DELETED, AuditEntityType.RESIDENT, saved.getId(),
        "Admin deleted resident avatar: " + saved.getUser().getEmail(), Map.of("residentProfileId", saved.getId(), "residentUserId", saved.getUser().getId()), servletRequest);
    return residentProfileMapper.toAdminResponse(saved);
  }

  private ResidentProfile findProfile(Long id) {
    return residentProfileRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resident profile not found"));
  }

  private ResidentProfile findByUser(Long userId) {
    return residentProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resident profile not found"));
  }

  private Apartment resolveAssignableApartment(Long apartmentId, Long currentProfileId) {
    if (apartmentId == null) {
      return null;
    }
    Apartment apartment = apartmentRepository.findById(apartmentId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Apartment not found"));
    boolean occupied = currentProfileId == null
        ? residentProfileRepository.existsByApartmentId(apartmentId)
        : residentProfileRepository.existsByApartmentIdAndIdNot(apartmentId, currentProfileId);
    if (occupied) {
      throw conflict("Apartment is already assigned to another resident");
    }
    boolean assignedToCurrentResident = currentProfileId != null && residentProfileRepository.existsByApartmentId(apartmentId);
    if (!assignedToCurrentResident && apartment.getStatus() != ApartmentStatus.VACANT) {
      throw conflict("Apartment is not available for assignment");
    }
    return apartment;
  }

  private void updateApartmentStatuses() {
    apartmentRepository.findAll().forEach(apartment -> {
      if (apartment.getStatus() != ApartmentStatus.MAINTENANCE) {
        apartment.setStatus(residentProfileRepository.existsByApartmentId(apartment.getId())
            ? ApartmentStatus.OCCUPIED
            : ApartmentStatus.VACANT);
      }
    });
  }

  private void rejectRestrictedResidentFields(ResidentProfileUpdateRequest request) {
    if (request.email() != null
        || request.role() != null
        || request.apartmentId() != null
        || request.notes() != null
        || request.enabled() != null
        || request.mustChangePassword() != null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Resident profile contains restricted fields");
    }
  }

  private String normalizeEmail(String email) {
    return clean(email).toLowerCase(Locale.ROOT);
  }

  private String normalizeLanguage(String language) {
    String value = cleanNullable(language);
    if (value == null || value.isBlank()) {
      return "uk";
    }
    if (!value.equals("uk") && !value.equals("en")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Unsupported language");
    }
    return value;
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }

  private ApiException conflict(String message) {
    return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message);
  }
}
