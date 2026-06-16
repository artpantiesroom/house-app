package com.houseapp.controller;

import com.houseapp.dto.response.AnnouncementResponse;
import com.houseapp.service.AnnouncementService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident/announcements")
public class ResidentAnnouncementController {
  private final AnnouncementService announcementService;

  public ResidentAnnouncementController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @GetMapping
  public List<AnnouncementResponse> list() {
    return announcementService.listForResident();
  }

  @GetMapping("/{id}")
  public AnnouncementResponse get(@PathVariable Long id) {
    return announcementService.getForResident(id);
  }
}
