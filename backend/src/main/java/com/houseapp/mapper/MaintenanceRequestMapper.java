package com.houseapp.mapper;

import com.houseapp.dto.response.admin.MaintenanceRequestAdminResponse;
import com.houseapp.dto.response.resident.MaintenanceRequestResidentResponse;
import com.houseapp.entity.Apartment;
import com.houseapp.entity.MaintenanceRequest;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRequestMapper {
  public MaintenanceRequestResidentResponse toResidentResponse(MaintenanceRequest request) {
    Apartment apartment = request.getApartment();
    return new MaintenanceRequestResidentResponse(
        request.getId(),
        request.getTitle(),
        request.getDescription(),
        request.getCategory(),
        request.getPriority(),
        request.getStatus(),
        request.getAdminResponse(),
        request.getCreatedAt(),
        request.getUpdatedAt(),
        request.getResolvedAt(),
        apartment == null ? null : apartment.getApartmentNumber()
    );
  }

  public MaintenanceRequestAdminResponse toAdminResponse(MaintenanceRequest request) {
    ResidentProfile profile = request.getResidentProfile();
    User user = profile.getUser();
    Apartment apartment = request.getApartment();
    return new MaintenanceRequestAdminResponse(
        request.getId(),
        request.getTitle(),
        request.getDescription(),
        request.getCategory(),
        request.getPriority(),
        request.getStatus(),
        request.getAdminResponse(),
        request.getInternalNotes(),
        request.getCreatedAt(),
        request.getUpdatedAt(),
        request.getResolvedAt(),
        profile.getId(),
        user.getName(),
        user.getEmail(),
        apartment == null ? null : apartment.getId(),
        apartment == null ? null : apartment.getApartmentNumber(),
        apartment == null ? null : apartment.getBuildingSection(),
        apartment == null ? null : apartment.getFloor()
    );
  }
}
