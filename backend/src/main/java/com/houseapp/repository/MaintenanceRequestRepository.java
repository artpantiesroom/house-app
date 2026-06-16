package com.houseapp.repository;

import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceRequest;
import com.houseapp.entity.MaintenanceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
  boolean existsByTitleIgnoreCaseAndResidentProfileId(String title, Long residentProfileId);

  List<MaintenanceRequest> findAllByResidentProfileIdOrderByCreatedAtDesc(Long residentProfileId);

  Optional<MaintenanceRequest> findByIdAndResidentProfileId(Long id, Long residentProfileId);

  @Query("""
      select m from MaintenanceRequest m
      join fetch m.residentProfile rp
      join fetch rp.user u
      left join fetch m.apartment a
      where (:status is null or m.status = :status)
        and (:category is null or m.category = :category)
        and (:priority is null or m.priority = :priority)
        and (:search is null
          or lower(m.title) like lower(concat('%', :search, '%'))
          or lower(m.description) like lower(concat('%', :search, '%'))
          or lower(coalesce(m.adminResponse, '')) like lower(concat('%', :search, '%'))
          or lower(u.name) like lower(concat('%', :search, '%'))
          or lower(u.email) like lower(concat('%', :search, '%'))
          or lower(coalesce(a.apartmentNumber, '')) like lower(concat('%', :search, '%')))
      order by
        case m.priority when com.houseapp.entity.MaintenancePriority.URGENT then 0
          when com.houseapp.entity.MaintenancePriority.HIGH then 1
          when com.houseapp.entity.MaintenancePriority.NORMAL then 2
          else 3 end,
        m.createdAt desc
      """)
  List<MaintenanceRequest> searchForAdmin(
      @Param("status") MaintenanceStatus status,
      @Param("category") MaintenanceCategory category,
      @Param("priority") MaintenancePriority priority,
      @Param("search") String search
  );
}
