package com.houseapp.service;

import com.houseapp.dto.request.admin.MaintenanceRequestUpdateRequest;
import com.houseapp.dto.request.resident.MaintenanceRequestCreateRequest;
import com.houseapp.dto.response.admin.MaintenanceRequestAdminResponse;
import com.houseapp.dto.response.resident.MaintenanceRequestResidentResponse;
import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceRequest;
import com.houseapp.entity.MaintenanceStatus;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.MaintenanceRequestMapper;
import com.houseapp.repository.MaintenanceRequestRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceRequestService {
  private final MaintenanceRequestRepository maintenanceRequestRepository;
  private final ResidentProfileRepository residentProfileRepository;
  private final MaintenanceRequestMapper maintenanceRequestMapper;

  public MaintenanceRequestService(
      MaintenanceRequestRepository maintenanceRequestRepository,
      ResidentProfileRepository residentProfileRepository,
      MaintenanceRequestMapper maintenanceRequestMapper
  ) {
    this.maintenanceRequestRepository = maintenanceRequestRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.maintenanceRequestMapper = maintenanceRequestMapper;
  }

  @Transactional(readOnly = true)
  public List<MaintenanceRequestResidentResponse> listForResident(UserPrincipal principal) {
    ResidentProfile profile = findProfileByUser(principal.getId());
    return maintenanceRequestRepository.findAllByResidentProfileIdOrderByCreatedAtDesc(profile.getId()).stream()
        .map(maintenanceRequestMapper::toResidentResponse)
        .toList();
  }

  @Transactional
  public MaintenanceRequestResidentResponse createForResident(UserPrincipal principal, MaintenanceRequestCreateRequest request) {
    ResidentProfile profile = findProfileByUser(principal.getId());
    MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
    maintenanceRequest.setResidentProfile(profile);
    maintenanceRequest.setApartment(profile.getApartment());
    maintenanceRequest.setTitle(clean(request.title()));
    maintenanceRequest.setDescription(clean(request.description()));
    maintenanceRequest.setCategory(request.category());
    maintenanceRequest.setPriority(MaintenancePriority.NORMAL);
    maintenanceRequest.setStatus(MaintenanceStatus.NEW);
    return maintenanceRequestMapper.toResidentResponse(maintenanceRequestRepository.save(maintenanceRequest));
  }

  @Transactional(readOnly = true)
  public MaintenanceRequestResidentResponse getForResident(UserPrincipal principal, Long id) {
    ResidentProfile profile = findProfileByUser(principal.getId());
    MaintenanceRequest request = maintenanceRequestRepository.findByIdAndResidentProfileId(id, profile.getId())
        .orElseThrow(this::notFound);
    return maintenanceRequestMapper.toResidentResponse(request);
  }

  @Transactional(readOnly = true)
  public List<MaintenanceRequestAdminResponse> listForAdmin(
      MaintenanceStatus status,
      MaintenanceCategory category,
      MaintenancePriority priority,
      String search
  ) {
    return maintenanceRequestRepository.searchForAdmin(status, category, priority, cleanNullable(search)).stream()
        .map(maintenanceRequestMapper::toAdminResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public MaintenanceRequestAdminResponse getForAdmin(Long id) {
    return maintenanceRequestMapper.toAdminResponse(findRequest(id));
  }

  @Transactional
  public MaintenanceRequestAdminResponse updateForAdmin(Long id, MaintenanceRequestUpdateRequest request) {
    MaintenanceRequest maintenanceRequest = findRequest(id);
    if (request.status() != null) {
      maintenanceRequest.setStatus(request.status());
      if (request.status() == MaintenanceStatus.RESOLVED && maintenanceRequest.getResolvedAt() == null) {
        maintenanceRequest.setResolvedAt(Instant.now());
      }
    }
    if (request.priority() != null) {
      maintenanceRequest.setPriority(request.priority());
    }
    if (request.adminResponse() != null) {
      maintenanceRequest.setAdminResponse(cleanNullable(request.adminResponse()));
    }
    if (request.internalNotes() != null) {
      maintenanceRequest.setInternalNotes(cleanNullable(request.internalNotes()));
    }
    return maintenanceRequestMapper.toAdminResponse(maintenanceRequestRepository.save(maintenanceRequest));
  }

  private MaintenanceRequest findRequest(Long id) {
    return maintenanceRequestRepository.findById(id).orElseThrow(this::notFound);
  }

  private ResidentProfile findProfileByUser(Long userId) {
    return residentProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resident profile not found"));
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Maintenance request not found");
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }
}
