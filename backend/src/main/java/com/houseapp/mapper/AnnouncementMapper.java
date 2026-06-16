package com.houseapp.mapper;

import com.houseapp.dto.response.AnnouncementResponse;
import com.houseapp.entity.Announcement;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementMapper {
  public AnnouncementResponse toResponse(Announcement announcement) {
    return new AnnouncementResponse(
        announcement.getId(),
        announcement.getTitleUk(),
        announcement.getTitleEn(),
        announcement.getBodyUk(),
        announcement.getBodyEn(),
        announcement.getCategory(),
        announcement.getPriority(),
        announcement.getStatus(),
        announcement.getPublishedAt(),
        announcement.getExpiresAt(),
        announcement.getCreatedBy().getId(),
        announcement.getCreatedBy().getEmail(),
        announcement.getCreatedAt(),
        announcement.getUpdatedAt()
    );
  }
}
