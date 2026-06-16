package com.houseapp.repository;

import com.houseapp.entity.BuildingContact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingContactRepository extends JpaRepository<BuildingContact, Long> {
  boolean existsByNameUkIgnoreCase(String nameUk);

  List<BuildingContact> findAllByOrderBySortOrderAscNameUkAsc();

  List<BuildingContact> findAllByActiveTrueOrderBySortOrderAscNameUkAsc();
}
