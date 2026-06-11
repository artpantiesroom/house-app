package com.houseapp.service;

import com.houseapp.dto.request.admin.ApartmentRequest;
import com.houseapp.dto.response.admin.ApartmentResponse;
import com.houseapp.entity.Apartment;
import com.houseapp.entity.ApartmentStatus;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.ApartmentMapper;
import com.houseapp.repository.ApartmentRepository;
import com.houseapp.repository.ResidentProfileRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApartmentService {
  private final ApartmentRepository apartmentRepository;
  private final ResidentProfileRepository residentProfileRepository;
  private final ApartmentMapper apartmentMapper;

  public ApartmentService(
      ApartmentRepository apartmentRepository,
      ResidentProfileRepository residentProfileRepository,
      ApartmentMapper apartmentMapper
  ) {
    this.apartmentRepository = apartmentRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.apartmentMapper = apartmentMapper;
  }

  @Transactional(readOnly = true)
  public List<ApartmentResponse> list() {
    return apartmentRepository.findAll().stream().map(apartmentMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ApartmentResponse get(Long id) {
    return apartmentMapper.toResponse(findApartment(id));
  }

  @Transactional
  public ApartmentResponse create(ApartmentRequest request) {
    String apartmentNumber = normalize(request.apartmentNumber());
    if (apartmentRepository.existsByApartmentNumberIgnoreCase(apartmentNumber)) {
      throw conflict("Apartment number is already used");
    }
    Apartment apartment = new Apartment();
    apply(apartment, request, apartmentNumber);
    return apartmentMapper.toResponse(apartmentRepository.save(apartment));
  }

  @Transactional
  public ApartmentResponse update(Long id, ApartmentRequest request) {
    Apartment apartment = findApartment(id);
    String apartmentNumber = normalize(request.apartmentNumber());
    apartmentRepository.findByApartmentNumberIgnoreCase(apartmentNumber)
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(existing -> {
          throw conflict("Apartment number is already used");
        });
    apply(apartment, request, apartmentNumber);
    return apartmentMapper.toResponse(apartmentRepository.save(apartment));
  }

  @Transactional
  public void delete(Long id) {
    Apartment apartment = findApartment(id);
    if (residentProfileRepository.existsByApartmentId(id)) {
      throw conflict("Apartment is assigned to a resident");
    }
    apartmentRepository.delete(apartment);
  }

  private void apply(Apartment apartment, ApartmentRequest request, String apartmentNumber) {
    apartment.setBuildingSection(normalize(request.buildingSection()));
    apartment.setFloor(request.floor());
    apartment.setApartmentNumber(apartmentNumber);
    apartment.setAreaSqM(request.areaSqM());
    apartment.setRooms(request.rooms());
    apartment.setStatus(request.status() == null ? ApartmentStatus.VACANT : request.status());
  }

  private Apartment findApartment(Long id) {
    return apartmentRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Apartment not found"));
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim();
  }

  private ApiException conflict(String message) {
    return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message);
  }
}
