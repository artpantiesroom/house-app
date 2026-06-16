package com.houseapp.service;

import com.houseapp.dto.request.admin.AnnouncementRequest;
import com.houseapp.dto.response.AnnouncementResponse;
import com.houseapp.entity.Announcement;
import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.AnnouncementMapper;
import com.houseapp.repository.AnnouncementRepository;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final UserRepository userRepository;
  private final AnnouncementMapper announcementMapper;

  public AnnouncementService(
      AnnouncementRepository announcementRepository,
      UserRepository userRepository,
      AnnouncementMapper announcementMapper
  ) {
    this.announcementRepository = announcementRepository;
    this.userRepository = userRepository;
    this.announcementMapper = announcementMapper;
  }

  @Transactional(readOnly = true)
  public List<AnnouncementResponse> listForAdmin(
      AnnouncementStatus status,
      AnnouncementCategory category,
      AnnouncementPriority priority,
      String search
  ) {
    return announcementRepository.searchForAdmin(status, category, priority, cleanNullable(search)).stream()
        .map(announcementMapper::toResponse)
        .toList();
  }

  @Transactional
  public AnnouncementResponse create(AnnouncementRequest request, UserPrincipal principal) {
    Announcement announcement = new Announcement();
    announcement.setCreatedBy(findUser(principal.getId()));
    applyRequest(announcement, request, true);
    return announcementMapper.toResponse(announcementRepository.save(announcement));
  }

  @Transactional(readOnly = true)
  public AnnouncementResponse getForAdmin(Long id) {
    return announcementMapper.toResponse(findAnnouncement(id));
  }

  @Transactional
  public AnnouncementResponse update(Long id, AnnouncementRequest request) {
    Announcement announcement = findAnnouncement(id);
    applyRequest(announcement, request, false);
    return announcementMapper.toResponse(announcementRepository.save(announcement));
  }

  @Transactional
  public AnnouncementResponse publish(Long id) {
    Announcement announcement = findAnnouncement(id);
    announcement.setStatus(AnnouncementStatus.PUBLISHED);
    if (announcement.getPublishedAt() == null) {
      announcement.setPublishedAt(Instant.now());
    }
    validateExpiresAt(announcement.getExpiresAt(), AnnouncementStatus.PUBLISHED, announcement.getPublishedAt());
    return announcementMapper.toResponse(announcementRepository.save(announcement));
  }

  @Transactional
  public AnnouncementResponse archive(Long id) {
    Announcement announcement = findAnnouncement(id);
    announcement.setStatus(AnnouncementStatus.ARCHIVED);
    return announcementMapper.toResponse(announcementRepository.save(announcement));
  }

  @Transactional(readOnly = true)
  public List<AnnouncementResponse> listForResident() {
    return announcementRepository.findVisibleForResident(Instant.now()).stream()
        .map(announcementMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AnnouncementResponse getForResident(Long id) {
    Instant now = Instant.now();
    Announcement announcement = findAnnouncement(id);
    if (announcement.getStatus() != AnnouncementStatus.PUBLISHED
        || (announcement.getExpiresAt() != null && announcement.getExpiresAt().isBefore(now))) {
      throw notFound();
    }
    return announcementMapper.toResponse(announcement);
  }

  private void applyRequest(Announcement announcement, AnnouncementRequest request, boolean creating) {
    AnnouncementStatus status = request.status();
    Instant publishedAt = announcement.getPublishedAt();
    if (status == AnnouncementStatus.PUBLISHED && publishedAt == null) {
      publishedAt = Instant.now();
    }
    if (status != AnnouncementStatus.PUBLISHED && creating) {
      publishedAt = null;
    }
    validateExpiresAt(request.expiresAt(), status, publishedAt);

    announcement.setTitleUk(clean(request.titleUk()));
    announcement.setTitleEn(cleanNullable(request.titleEn()));
    announcement.setBodyUk(clean(request.bodyUk()));
    announcement.setBodyEn(cleanNullable(request.bodyEn()));
    announcement.setCategory(request.category());
    announcement.setPriority(request.priority());
    announcement.setStatus(status);
    announcement.setPublishedAt(publishedAt);
    announcement.setExpiresAt(request.expiresAt());
  }

  private void validateExpiresAt(Instant expiresAt, AnnouncementStatus status, Instant publishedAt) {
    if (expiresAt == null) {
      return;
    }
    Instant baseline = publishedAt == null ? Instant.now() : publishedAt;
    if (status == AnnouncementStatus.PUBLISHED && !expiresAt.isAfter(baseline)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Expiration must be after publication time");
    }
  }

  private Announcement findAnnouncement(Long id) {
    return announcementRepository.findById(id).orElseThrow(this::notFound);
  }

  private User findUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Announcement not found");
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }
}
