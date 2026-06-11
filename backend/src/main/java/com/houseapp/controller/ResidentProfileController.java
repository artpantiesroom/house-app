package com.houseapp.controller;

import com.houseapp.dto.request.resident.ResidentProfileUpdateRequest;
import com.houseapp.dto.response.resident.ResidentProfileResponse;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.ResidentProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident/profile")
public class ResidentProfileController {
  private final ResidentProfileService residentProfileService;

  public ResidentProfileController(ResidentProfileService residentProfileService) {
    this.residentProfileService = residentProfileService;
  }

  @GetMapping
  public ResidentProfileResponse get(@AuthenticationPrincipal UserPrincipal principal) {
    return residentProfileService.getOwnProfile(principal);
  }

  @PutMapping
  public ResidentProfileResponse update(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody ResidentProfileUpdateRequest request
  ) {
    return residentProfileService.updateOwnProfile(principal, request);
  }
}
