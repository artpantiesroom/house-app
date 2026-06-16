package com.houseapp.mapper;

import com.houseapp.dto.response.admin.AdminResidentResponse;
import com.houseapp.dto.response.resident.ResidentProfileResponse;
import com.houseapp.entity.Apartment;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ResidentProfileMapper {
  private final com.houseapp.service.AvatarStorageService avatarStorageService;

  public ResidentProfileMapper(com.houseapp.service.AvatarStorageService avatarStorageService) {
    this.avatarStorageService = avatarStorageService;
  }

  public AdminResidentResponse toAdminResponse(ResidentProfile profile) {
    User user = profile.getUser();
    Apartment apartment = profile.getApartment();
    return new AdminResidentResponse(
        profile.getId(),
        user.getId(),
        user.getName(),
        user.getEmail(),
        profile.getPhone(),
        apartment == null ? null : apartment.getId(),
        apartment == null ? null : apartment.getApartmentNumber(),
        apartment == null ? null : apartment.getBuildingSection(),
        apartment == null ? null : apartment.getFloor(),
        user.isEnabled(),
        user.isMustChangePassword(),
        user.getPreferredLanguage(),
        profile.getEmergencyContactName(),
        profile.getEmergencyContactPhone(),
        profile.getAvatarPath(),
        avatarStorageService.avatarUrl(profile.getAvatarPath()),
        profile.getNotes(),
        profile.getCreatedAt(),
        profile.getUpdatedAt()
    );
  }

  public ResidentProfileResponse toResidentResponse(ResidentProfile profile) {
    User user = profile.getUser();
    Apartment apartment = profile.getApartment();
    return new ResidentProfileResponse(
        profile.getId(),
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole().name(),
        user.getPreferredLanguage(),
        profile.getPhone(),
        apartment == null ? null : apartment.getId(),
        apartment == null ? null : apartment.getApartmentNumber(),
        apartment == null ? null : apartment.getBuildingSection(),
        apartment == null ? null : apartment.getFloor(),
        profile.getEmergencyContactName(),
        profile.getEmergencyContactPhone(),
        profile.getAvatarPath(),
        avatarStorageService.avatarUrl(profile.getAvatarPath()),
        profile.getCreatedAt(),
        profile.getUpdatedAt()
    );
  }
}
