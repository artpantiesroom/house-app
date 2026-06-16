package com.houseapp.controller;

import com.houseapp.dto.request.admin.BuildingContactRequest;
import com.houseapp.dto.response.BuildingContactResponse;
import com.houseapp.service.BuildingContactService;
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
@RequestMapping("/api/admin/contacts")
public class AdminBuildingContactController {
  private final BuildingContactService buildingContactService;

  public AdminBuildingContactController(BuildingContactService buildingContactService) {
    this.buildingContactService = buildingContactService;
  }

  @GetMapping
  public List<BuildingContactResponse> list() {
    return buildingContactService.listForAdmin();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BuildingContactResponse create(@Valid @RequestBody BuildingContactRequest request) {
    return buildingContactService.create(request);
  }

  @GetMapping("/{id}")
  public BuildingContactResponse get(@PathVariable Long id) {
    return buildingContactService.getForAdmin(id);
  }

  @PutMapping("/{id}")
  public BuildingContactResponse update(@PathVariable Long id, @Valid @RequestBody BuildingContactRequest request) {
    return buildingContactService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable Long id) {
    buildingContactService.deactivate(id);
  }
}
