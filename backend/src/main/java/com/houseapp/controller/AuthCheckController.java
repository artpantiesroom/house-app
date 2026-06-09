package com.houseapp.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthCheckController {
  @GetMapping("/api/admin/auth-check")
  public Map<String, String> admin() {
    return Map.of("scope", "ADMIN");
  }

  @GetMapping("/api/resident/auth-check")
  public Map<String, String> resident() {
    return Map.of("scope", "RESIDENT");
  }
}
