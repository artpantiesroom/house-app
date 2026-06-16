package com.houseapp.service;

import com.houseapp.dto.request.admin.BuildingContactRequest;
import com.houseapp.dto.response.BuildingContactResponse;
import com.houseapp.entity.BuildingContact;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.BuildingContactMapper;
import com.houseapp.repository.BuildingContactRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingContactService {
  private final BuildingContactRepository buildingContactRepository;
  private final BuildingContactMapper buildingContactMapper;

  public BuildingContactService(
      BuildingContactRepository buildingContactRepository,
      BuildingContactMapper buildingContactMapper
  ) {
    this.buildingContactRepository = buildingContactRepository;
    this.buildingContactMapper = buildingContactMapper;
  }

  @Transactional(readOnly = true)
  public List<BuildingContactResponse> listForAdmin() {
    return buildingContactRepository.findAllByOrderBySortOrderAscNameUkAsc().stream()
        .map(buildingContactMapper::toResponse)
        .toList();
  }

  @Transactional
  public BuildingContactResponse create(BuildingContactRequest request) {
    BuildingContact contact = new BuildingContact();
    applyRequest(contact, request);
    return buildingContactMapper.toResponse(buildingContactRepository.save(contact));
  }

  @Transactional(readOnly = true)
  public BuildingContactResponse getForAdmin(Long id) {
    return buildingContactMapper.toResponse(findContact(id));
  }

  @Transactional
  public BuildingContactResponse update(Long id, BuildingContactRequest request) {
    BuildingContact contact = findContact(id);
    applyRequest(contact, request);
    return buildingContactMapper.toResponse(buildingContactRepository.save(contact));
  }

  @Transactional
  public void deactivate(Long id) {
    BuildingContact contact = findContact(id);
    contact.setActive(false);
    buildingContactRepository.save(contact);
  }

  @Transactional(readOnly = true)
  public List<BuildingContactResponse> listForResident() {
    return buildingContactRepository.findAllByActiveTrueOrderBySortOrderAscNameUkAsc().stream()
        .map(buildingContactMapper::toResponse)
        .toList();
  }

  private void applyRequest(BuildingContact contact, BuildingContactRequest request) {
    String phone = cleanNullable(request.phone());
    String email = cleanNullable(request.email());
    if (phone == null && email == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Contact requires phone or email");
    }
    contact.setNameUk(clean(request.nameUk()));
    contact.setNameEn(cleanNullable(request.nameEn()));
    contact.setRoleUk(clean(request.roleUk()));
    contact.setRoleEn(cleanNullable(request.roleEn()));
    contact.setDepartmentUk(cleanNullable(request.departmentUk()));
    contact.setDepartmentEn(cleanNullable(request.departmentEn()));
    contact.setPhone(phone);
    contact.setEmail(email == null ? null : email.toLowerCase());
    contact.setAvailabilityUk(cleanNullable(request.availabilityUk()));
    contact.setAvailabilityEn(cleanNullable(request.availabilityEn()));
    contact.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    contact.setActive(request.active() == null || request.active());
  }

  private BuildingContact findContact(Long id) {
    return buildingContactRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Contact not found"));
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }
}
