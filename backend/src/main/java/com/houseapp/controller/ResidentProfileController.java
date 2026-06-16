package com.houseapp.controller;

import com.houseapp.dto.request.resident.ResidentProfileUpdateRequest;
import com.houseapp.dto.response.resident.ResidentProfileResponse;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.ResidentProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

  @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResidentProfileResponse uploadAvatar(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam("file") MultipartFile file,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.uploadOwnAvatar(principal, file, servletRequest);
  }

  @DeleteMapping("/avatar")
  public ResidentProfileResponse deleteAvatar(
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.deleteOwnAvatar(principal, servletRequest);
  }
}
