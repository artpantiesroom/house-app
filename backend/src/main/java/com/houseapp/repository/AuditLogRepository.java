package com.houseapp.repository;

import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.AuditLog;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  boolean existsByActionAndEntityTypeAndEntityIdAndSummary(
      AuditAction action,
      AuditEntityType entityType,
      Long entityId,
      String summary
  );

  @Query("""
      select a from AuditLog a
      left join fetch a.actorUser
      where (:action is null or a.action = :action)
        and (:entityType is null or a.entityType = :entityType)
        and (:actorUserId is null or a.actorUser.id = :actorUserId)
        and (:entityId is null or a.entityId = :entityId)
        and (:dateFrom is null or a.createdAt >= :dateFrom)
        and (:dateTo is null or a.createdAt <= :dateTo)
        and (:search is null
          or lower(coalesce(a.actorEmail, '')) like lower(concat('%', :search, '%'))
          or lower(a.summary) like lower(concat('%', :search, '%'))
          or lower(coalesce(a.metadataJson, '')) like lower(concat('%', :search, '%'))
          or lower(cast(a.action as string)) like lower(concat('%', :search, '%'))
          or lower(cast(a.entityType as string)) like lower(concat('%', :search, '%')))
      order by a.createdAt desc, a.id desc
      """)
  List<AuditLog> search(
      @Param("action") AuditAction action,
      @Param("entityType") AuditEntityType entityType,
      @Param("actorUserId") Long actorUserId,
      @Param("entityId") Long entityId,
      @Param("dateFrom") Instant dateFrom,
      @Param("dateTo") Instant dateTo,
      @Param("search") String search
  );
}
