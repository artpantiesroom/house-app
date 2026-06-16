package com.houseapp.repository;

import com.houseapp.entity.SecurityIncident;
import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, Long> {
  boolean existsByTitleIgnoreCase(String title);

  @Query("""
      select i from SecurityIncident i
      left join fetch i.reportedBy
      left join fetch i.assignedTo
      left join fetch i.relatedAuditLog
      where (:severity is null or i.severity = :severity)
        and (:status is null or i.status = :status)
        and (:category is null or i.category = :category)
        and (:assignedToUserId is null or i.assignedTo.id = :assignedToUserId)
        and (:dateFrom is null or i.createdAt >= :dateFrom)
        and (:dateTo is null or i.createdAt <= :dateTo)
        and (:search is null
          or lower(i.title) like lower(concat('%', :search, '%'))
          or lower(i.description) like lower(concat('%', :search, '%'))
          or lower(coalesce(i.resolutionNotes, '')) like lower(concat('%', :search, '%'))
          or lower(coalesce(i.assignedTo.email, '')) like lower(concat('%', :search, '%')))
      order by
        case i.status when com.houseapp.entity.SecurityIncidentStatus.OPEN then 0
          when com.houseapp.entity.SecurityIncidentStatus.INVESTIGATING then 1
          when com.houseapp.entity.SecurityIncidentStatus.RESOLVED then 2
          else 3 end,
        i.createdAt desc,
        i.id desc
      """)
  List<SecurityIncident> search(
      @Param("severity") SecurityIncidentSeverity severity,
      @Param("status") SecurityIncidentStatus status,
      @Param("category") SecurityIncidentCategory category,
      @Param("assignedToUserId") Long assignedToUserId,
      @Param("dateFrom") Instant dateFrom,
      @Param("dateTo") Instant dateTo,
      @Param("search") String search
  );
}
