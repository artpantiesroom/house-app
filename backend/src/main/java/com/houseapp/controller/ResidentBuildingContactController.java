package com.houseapp.controller;

import com.houseapp.dto.response.BuildingContactResponse;
import com.houseapp.service.BuildingContactService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident/contacts")
public class ResidentBuildingContactController {
  private final BuildingContactService buildingContactService;

  public ResidentBuildingContactController(BuildingContactService buildingContactService) {
    this.buildingContactService = buildingContactService;
  }

  @GetMapping
  public List<BuildingContactResponse> list() {
    return buildingContactService.listForResident();
  }
}
