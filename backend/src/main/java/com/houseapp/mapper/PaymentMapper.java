package com.houseapp.mapper;

import com.houseapp.dto.response.admin.PaymentAdminResponse;
import com.houseapp.dto.response.resident.PaymentResidentResponse;
import com.houseapp.entity.Apartment;
import com.houseapp.entity.Payment;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
  public PaymentResidentResponse toResidentResponse(Payment payment) {
    Apartment apartment = payment.getApartment();
    return new PaymentResidentResponse(
        payment.getId(),
        payment.getType(),
        payment.getStatus(),
        payment.getAmountMinor(),
        payment.getCurrency(),
        payment.getPeriodYear(),
        payment.getPeriodMonth(),
        payment.getTitleUk(),
        payment.getTitleEn(),
        payment.getDescriptionUk(),
        payment.getDescriptionEn(),
        payment.getDueDate(),
        payment.getPaidAt(),
        payment.getCreatedAt(),
        payment.getUpdatedAt(),
        apartment == null ? null : apartment.getApartmentNumber()
    );
  }

  public PaymentAdminResponse toAdminResponse(Payment payment) {
    ResidentProfile profile = payment.getResidentProfile();
    User user = profile.getUser();
    Apartment apartment = payment.getApartment();
    return new PaymentAdminResponse(
        payment.getId(),
        profile.getId(),
        user.getName(),
        user.getEmail(),
        apartment == null ? null : apartment.getId(),
        apartment == null ? null : apartment.getApartmentNumber(),
        apartment == null ? null : apartment.getBuildingSection(),
        apartment == null ? null : apartment.getFloor(),
        payment.getType(),
        payment.getStatus(),
        payment.getAmountMinor(),
        payment.getCurrency(),
        payment.getPeriodYear(),
        payment.getPeriodMonth(),
        payment.getTitleUk(),
        payment.getTitleEn(),
        payment.getDescriptionUk(),
        payment.getDescriptionEn(),
        payment.getDueDate(),
        payment.getPaidAt(),
        payment.getCreatedAt(),
        payment.getUpdatedAt()
    );
  }
}
