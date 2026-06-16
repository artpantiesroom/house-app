package com.houseapp.controller;

import com.houseapp.dto.request.admin.MaintenanceRequestUpdateRequest;
import com.houseapp.dto.response.admin.MaintenanceRequestAdminResponse;
import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceStatus;
import com.houseapp.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/maintenance-requests")
public class AdminMaintenanceRequestController {
  private final MaintenanceRequestService maintenanceRequestService;

  public AdminMaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
    this.maintenanceRequestService = maintenanceRequestService;
  }

  @GetMapping
  public List<MaintenanceRequestAdminResponse> list(
      @RequestParam(required = false) MaintenanceStatus status,
      @RequestParam(required = false) MaintenanceCategory category,
      @RequestParam(required = false) MaintenancePriority priority,
      @RequestParam(required = false) String search
  ) {
    return maintenanceRequestService.listForAdmin(status, category, priority, search);
  }

  @GetMapping("/{id}")
  public MaintenanceRequestAdminResponse get(@PathVariable Long id) {
    return maintenanceRequestService.getForAdmin(id);
  }

  @PatchMapping("/{id}")
  public MaintenanceRequestAdminResponse update(
      @PathVariable Long id,
      @Valid @RequestBody MaintenanceRequestUpdateRequest request
  ) {
    return maintenanceRequestService.updateForAdmin(id, request);
  }
}
