package com.houseapp.repository;

import com.houseapp.entity.ResidentProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentProfileRepository extends JpaRepository<ResidentProfile, Long> {
  @EntityGraph(attributePaths = {"user", "apartment"})
  List<ResidentProfile> findAllByUserRoleOrderByCreatedAtDesc(com.houseapp.entity.Role role);

  @EntityGraph(attributePaths = {"user", "apartment"})
  Optional<ResidentProfile> findById(Long id);

  @EntityGraph(attributePaths = {"user", "apartment"})
  Optional<ResidentProfile> findByUserId(Long userId);

  boolean existsByApartmentId(Long apartmentId);

  boolean existsByApartmentIdAndIdNot(Long apartmentId, Long id);
}
