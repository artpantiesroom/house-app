package com.houseapp.repository;

import com.houseapp.entity.Announcement;
import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
  boolean existsByTitleUkIgnoreCase(String titleUk);

  @Query("""
      select a from Announcement a
      where (:status is null or a.status = :status)
        and (:category is null or a.category = :category)
        and (:priority is null or a.priority = :priority)
        and (:search is null
          or lower(a.titleUk) like lower(concat('%', :search, '%'))
          or lower(coalesce(a.titleEn, '')) like lower(concat('%', :search, '%'))
          or lower(a.bodyUk) like lower(concat('%', :search, '%'))
          or lower(coalesce(a.bodyEn, '')) like lower(concat('%', :search, '%')))
      order by
        case a.priority when com.houseapp.entity.AnnouncementPriority.URGENT then 0
          when com.houseapp.entity.AnnouncementPriority.HIGH then 1
          when com.houseapp.entity.AnnouncementPriority.NORMAL then 2
          else 3 end,
        coalesce(a.publishedAt, a.createdAt) desc
      """)
  List<Announcement> searchForAdmin(
      @Param("status") AnnouncementStatus status,
      @Param("category") AnnouncementCategory category,
      @Param("priority") AnnouncementPriority priority,
      @Param("search") String search
  );

  @Query("""
      select a from Announcement a
      where a.status = com.houseapp.entity.AnnouncementStatus.PUBLISHED
        and (a.expiresAt is null or a.expiresAt >= :now)
      order by
        case a.priority when com.houseapp.entity.AnnouncementPriority.URGENT then 0
          when com.houseapp.entity.AnnouncementPriority.HIGH then 1
          when com.houseapp.entity.AnnouncementPriority.NORMAL then 2
          else 3 end,
        a.publishedAt desc
      """)
  List<Announcement> findVisibleForResident(@Param("now") Instant now);
}
