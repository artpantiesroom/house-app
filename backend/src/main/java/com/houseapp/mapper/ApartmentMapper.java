package com.houseapp.mapper;

import com.houseapp.dto.response.admin.ApartmentResponse;
import com.houseapp.entity.Apartment;
import org.springframework.stereotype.Component;

@Component
public class ApartmentMapper {
  public ApartmentResponse toResponse(Apartment apartment) {
    return new ApartmentResponse(
        apartment.getId(),
        apartment.getBuildingSection(),
        apartment.getFloor(),
        apartment.getApartmentNumber(),
        apartment.getAreaSqM(),
        apartment.getRooms(),
        apartment.getStatus(),
        apartment.getCreatedAt(),
        apartment.getUpdatedAt()
    );
  }
}
