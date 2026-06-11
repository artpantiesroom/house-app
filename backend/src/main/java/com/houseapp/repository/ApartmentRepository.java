package com.houseapp.repository;

import com.houseapp.entity.Apartment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
  boolean existsByApartmentNumberIgnoreCase(String apartmentNumber);

  Optional<Apartment> findByApartmentNumberIgnoreCase(String apartmentNumber);
}
