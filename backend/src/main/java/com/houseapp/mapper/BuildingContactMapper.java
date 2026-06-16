package com.houseapp.mapper;

import com.houseapp.dto.response.BuildingContactResponse;
import com.houseapp.entity.BuildingContact;
import org.springframework.stereotype.Component;

@Component
public class BuildingContactMapper {
  public BuildingContactResponse toResponse(BuildingContact contact) {
    return new BuildingContactResponse(
        contact.getId(),
        contact.getNameUk(),
        contact.getNameEn(),
        contact.getRoleUk(),
        contact.getRoleEn(),
        contact.getDepartmentUk(),
        contact.getDepartmentEn(),
        contact.getPhone(),
        contact.getEmail(),
        contact.getAvailabilityUk(),
        contact.getAvailabilityEn(),
        contact.getSortOrder(),
        contact.isActive(),
        contact.getCreatedAt(),
        contact.getUpdatedAt()
    );
  }
}
