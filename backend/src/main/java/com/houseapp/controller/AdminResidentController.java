package com.houseapp.controller;

import com.houseapp.dto.request.admin.AdminResidentCreateRequest;
import com.houseapp.dto.request.admin.AdminResidentUpdateRequest;
import com.houseapp.dto.response.admin.AdminResidentResponse;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.ResidentProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/residents")
public class AdminResidentController {
  private final ResidentProfileService residentProfileService;

  public AdminResidentController(ResidentProfileService residentProfileService) {
    this.residentProfileService = residentProfileService;
  }

  @GetMapping
  public List<AdminResidentResponse> list() {
    return residentProfileService.listForAdmin();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminResidentResponse create(
      @Valid @RequestBody AdminResidentCreateRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.create(request, principal, servletRequest);
  }

  @GetMapping("/{id}")
  public AdminResidentResponse get(@PathVariable Long id) {
    return residentProfileService.getForAdmin(id);
  }

  @PutMapping("/{id}")
  public AdminResidentResponse update(
      @PathVariable Long id,
      @Valid @RequestBody AdminResidentUpdateRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.update(id, request, principal, servletRequest);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    residentProfileService.deactivate(id, principal, servletRequest);
  }

  @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AdminResidentResponse uploadAvatar(
      @PathVariable Long id,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.uploadAvatarForAdmin(id, file, principal, servletRequest);
  }

  @DeleteMapping("/{id}/avatar")
  public AdminResidentResponse deleteAvatar(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return residentProfileService.deleteAvatarForAdmin(id, principal, servletRequest);
  }
}
