package com.houseapp.controller;

import com.houseapp.dto.request.admin.AnnouncementRequest;
import com.houseapp.dto.response.AnnouncementResponse;
import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.AnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {
  private final AnnouncementService announcementService;

  public AdminAnnouncementController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @GetMapping
  public List<AnnouncementResponse> list(
      @RequestParam(required = false) AnnouncementStatus status,
      @RequestParam(required = false) AnnouncementCategory category,
      @RequestParam(required = false) AnnouncementPriority priority,
      @RequestParam(required = false) String search
  ) {
    return announcementService.listForAdmin(status, category, priority, search);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AnnouncementResponse create(
      @Valid @RequestBody AnnouncementRequest request,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    return announcementService.create(request, principal);
  }

  @GetMapping("/{id}")
  public AnnouncementResponse get(@PathVariable Long id) {
    return announcementService.getForAdmin(id);
  }

  @PutMapping("/{id}")
  public AnnouncementResponse update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
    return announcementService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public AnnouncementResponse archiveOnDelete(@PathVariable Long id) {
    return announcementService.archive(id);
  }

  @PatchMapping("/{id}/publish")
  public AnnouncementResponse publish(@PathVariable Long id) {
    return announcementService.publish(id);
  }

  @PatchMapping("/{id}/archive")
  public AnnouncementResponse archive(@PathVariable Long id) {
    return announcementService.archive(id);
  }
}
