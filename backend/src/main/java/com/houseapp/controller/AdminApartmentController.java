package com.houseapp.controller;

import com.houseapp.dto.request.admin.ApartmentRequest;
import com.houseapp.dto.response.admin.ApartmentResponse;
import com.houseapp.service.ApartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/apartments")
public class AdminApartmentController {
  private final ApartmentService apartmentService;

  public AdminApartmentController(ApartmentService apartmentService) {
    this.apartmentService = apartmentService;
  }

  @GetMapping
  public List<ApartmentResponse> list() {
    return apartmentService.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApartmentResponse create(@Valid @RequestBody ApartmentRequest request) {
    return apartmentService.create(request);
  }

  @GetMapping("/{id}")
  public ApartmentResponse get(@PathVariable Long id) {
    return apartmentService.get(id);
  }

  @PutMapping("/{id}")
  public ApartmentResponse update(@PathVariable Long id, @Valid @RequestBody ApartmentRequest request) {
    return apartmentService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    apartmentService.delete(id);
  }
}
