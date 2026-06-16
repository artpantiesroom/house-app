package com.houseapp.controller;

import com.houseapp.service.AvatarStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files/avatars")
public class AvatarFileController {
  private final AvatarStorageService avatarStorageService;

  public AvatarFileController(AvatarStorageService avatarStorageService) {
    this.avatarStorageService = avatarStorageService;
  }

  @GetMapping("/{filename}")
  public ResponseEntity<Resource> get(@PathVariable String filename) {
    Resource resource = avatarStorageService.load(filename);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .header("Content-Type", avatarStorageService.contentType(filename))
        .body(resource);
  }
}
