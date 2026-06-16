package com.houseapp.controller;

import com.houseapp.dto.request.resident.MaintenanceRequestCreateRequest;
import com.houseapp.dto.response.resident.MaintenanceRequestResidentResponse;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident/maintenance-requests")
public class ResidentMaintenanceRequestController {
  private final MaintenanceRequestService maintenanceRequestService;

  public ResidentMaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
    this.maintenanceRequestService = maintenanceRequestService;
  }

  @GetMapping
  public List<MaintenanceRequestResidentResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
    return maintenanceRequestService.listForResident(principal);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MaintenanceRequestResidentResponse create(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody MaintenanceRequestCreateRequest request
  ) {
    return maintenanceRequestService.createForResident(principal, request);
  }

  @GetMapping("/{id}")
  public MaintenanceRequestResidentResponse get(
      @AuthenticationPrincipal UserPrincipal principal,
      @PathVariable Long id
  ) {
    return maintenanceRequestService.getForResident(principal, id);
  }
}
