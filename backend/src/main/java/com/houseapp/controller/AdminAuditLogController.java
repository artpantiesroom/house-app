package com.houseapp.controller;

import com.houseapp.dto.response.admin.AuditLogResponse;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.service.AuditLogService;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {
  private final AuditLogService auditLogService;

  public AdminAuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public List<AuditLogResponse> list(
      @RequestParam(required = false) AuditAction action,
      @RequestParam(required = false) AuditEntityType entityType,
      @RequestParam(required = false) Long actorUserId,
      @RequestParam(required = false) Long entityId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
      @RequestParam(required = false) String search
  ) {
    return auditLogService.list(action, entityType, actorUserId, entityId, dateFrom, dateTo, search);
  }

  @GetMapping("/{id}")
  public AuditLogResponse get(@PathVariable Long id) {
    return auditLogService.get(id);
  }
}
