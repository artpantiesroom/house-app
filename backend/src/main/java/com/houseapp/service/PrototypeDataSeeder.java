package com.houseapp.service;

import com.houseapp.entity.Apartment;
import com.houseapp.entity.ApartmentStatus;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.Role;
import com.houseapp.entity.User;
import com.houseapp.repository.ApartmentRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PrototypeDataSeeder implements ApplicationRunner {
  private final UserRepository userRepository;
  private final ApartmentRepository apartmentRepository;
  private final ResidentProfileRepository residentProfileRepository;
  private final PasswordEncoder passwordEncoder;

  public PrototypeDataSeeder(
      UserRepository userRepository,
      ApartmentRepository apartmentRepository,
      ResidentProfileRepository residentProfileRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.apartmentRepository = apartmentRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedUser("Administrator", "admin@house.com", "Admin123!", Role.ADMIN, false);
    User demoResident = seedUser("Demo Resident", "resident@house.com", "Resident123!", Role.RESIDENT, false);

    Apartment apartment101 = seedApartment("A", 1, "101", "48.50", 2, ApartmentStatus.OCCUPIED);
    seedApartment("A", 1, "102", "39.20", 1, ApartmentStatus.OCCUPIED);
    seedApartment("A", 2, "201", "64.80", 3, ApartmentStatus.OCCUPIED);
    seedApartment("B", 2, "B-202", "58.40", 2, ApartmentStatus.OCCUPIED);
    seedApartment("B", 3, "B-301", "73.10", 3, ApartmentStatus.OCCUPIED);

    seedResidentProfile(demoResident, apartment101, "+380501112233", "Олена Демчук", "+380501112244", null, "Seeded prototype resident profile.");
    seedDemoResident("Olena Kovalenko", "olena.resident@house.com", "TempResident1!", "102", "+380671234501");
    seedDemoResident("Andrii Shevchenko", "andrii.resident@house.com", "TempResident1!", "201", "+380671234502");
    seedDemoResident("Iryna Melnyk", "iryna.resident@house.com", "TempResident1!", "B-202", "+380671234503");
    seedDemoResident("Taras Bondar", "taras.resident@house.com", "TempResident1!", "B-301", "+380671234504");
  }

  private User seedUser(String name, String email, String rawPassword, Role role, boolean mustChangePassword) {
    String normalizedEmail = email.toLowerCase(Locale.ROOT);
    return userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
      User user = new User();
      user.setName(name);
      user.setEmail(normalizedEmail);
      user.setPasswordHash(passwordEncoder.encode(rawPassword));
      user.setRole(role);
      user.setPreferredLanguage("uk");
      user.setMustChangePassword(mustChangePassword);
      user.setEnabled(true);
      return userRepository.save(user);
    });
  }

  private Apartment seedApartment(
      String buildingSection,
      int floor,
      String apartmentNumber,
      String areaSqM,
      int rooms,
      ApartmentStatus status
  ) {
    return apartmentRepository.findByApartmentNumberIgnoreCase(apartmentNumber).orElseGet(() -> {
      Apartment apartment = new Apartment();
      apartment.setBuildingSection(buildingSection);
      apartment.setFloor(floor);
      apartment.setApartmentNumber(apartmentNumber);
      apartment.setAreaSqM(new BigDecimal(areaSqM));
      apartment.setRooms(rooms);
      apartment.setStatus(status);
      return apartmentRepository.save(apartment);
    });
  }

  private void seedDemoResident(String name, String email, String rawPassword, String apartmentNumber, String phone) {
    User user = seedUser(name, email, rawPassword, Role.RESIDENT, true);
    Apartment apartment = apartmentRepository.findByApartmentNumberIgnoreCase(apartmentNumber).orElse(null);
    seedResidentProfile(user, apartment, phone, null, null, null, "Prototype demo resident. Temporary password requires replacement.");
  }

  private void seedResidentProfile(
      User user,
      Apartment apartment,
      String phone,
      String emergencyContactName,
      String emergencyContactPhone,
      String avatarPath,
      String notes
  ) {
    if (residentProfileRepository.findByUserId(user.getId()).isPresent()) {
      return;
    }
    ResidentProfile profile = new ResidentProfile();
    profile.setUser(user);
    profile.setApartment(apartment);
    profile.setPhone(phone);
    profile.setEmergencyContactName(emergencyContactName);
    profile.setEmergencyContactPhone(emergencyContactPhone);
    profile.setAvatarPath(avatarPath);
    profile.setNotes(notes);
    residentProfileRepository.save(profile);
  }
}
