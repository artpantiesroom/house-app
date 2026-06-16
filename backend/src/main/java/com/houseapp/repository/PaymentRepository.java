package com.houseapp.repository;

import com.houseapp.entity.Payment;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  boolean existsByResidentProfileIdAndTypeAndPeriodYearAndPeriodMonthAndTitleUkIgnoreCase(
      Long residentProfileId,
      PaymentType type,
      Integer periodYear,
      Integer periodMonth,
      String titleUk
  );

  @Query("""
      select p from Payment p
      left join fetch p.apartment
      where p.residentProfile.id = :residentProfileId
        and (:status is null or p.status = :status)
        and (:type is null or p.type = :type)
        and (:periodYear is null or p.periodYear = :periodYear)
        and (:periodMonth is null or p.periodMonth = :periodMonth)
      order by
        case p.status when com.houseapp.entity.PaymentStatus.OVERDUE then 0
          when com.houseapp.entity.PaymentStatus.PENDING then 1
          when com.houseapp.entity.PaymentStatus.PAID then 2
          else 3 end,
        case when p.status in (com.houseapp.entity.PaymentStatus.OVERDUE, com.houseapp.entity.PaymentStatus.PENDING) then p.dueDate end asc,
        p.dueDate desc
      """)
  List<Payment> searchForResident(
      @Param("residentProfileId") Long residentProfileId,
      @Param("status") PaymentStatus status,
      @Param("type") PaymentType type,
      @Param("periodYear") Integer periodYear,
      @Param("periodMonth") Integer periodMonth
  );

  Optional<Payment> findByIdAndResidentProfileId(Long id, Long residentProfileId);

  @Query("""
      select p from Payment p
      join fetch p.residentProfile rp
      join fetch rp.user u
      left join fetch p.apartment a
      where (:status is null or p.status = :status)
        and (:type is null or p.type = :type)
        and (:residentId is null or rp.id = :residentId)
        and (:apartmentId is null or a.id = :apartmentId)
        and (:periodYear is null or p.periodYear = :periodYear)
        and (:periodMonth is null or p.periodMonth = :periodMonth)
        and (:search is null
          or lower(p.titleUk) like lower(concat('%', :search, '%'))
          or lower(coalesce(p.titleEn, '')) like lower(concat('%', :search, '%'))
          or lower(coalesce(p.descriptionUk, '')) like lower(concat('%', :search, '%'))
          or lower(coalesce(p.descriptionEn, '')) like lower(concat('%', :search, '%'))
          or lower(u.name) like lower(concat('%', :search, '%'))
          or lower(u.email) like lower(concat('%', :search, '%'))
          or lower(coalesce(a.apartmentNumber, '')) like lower(concat('%', :search, '%')))
      order by
        case p.status when com.houseapp.entity.PaymentStatus.OVERDUE then 0
          when com.houseapp.entity.PaymentStatus.PENDING then 1
          when com.houseapp.entity.PaymentStatus.PAID then 2
          else 3 end,
        p.dueDate desc
      """)
  List<Payment> searchForAdmin(
      @Param("status") PaymentStatus status,
      @Param("type") PaymentType type,
      @Param("residentId") Long residentId,
      @Param("apartmentId") Long apartmentId,
      @Param("periodYear") Integer periodYear,
      @Param("periodMonth") Integer periodMonth,
      @Param("search") String search
  );
}
