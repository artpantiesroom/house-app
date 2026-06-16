package com.houseapp.service;

import com.houseapp.entity.Apartment;
import com.houseapp.entity.ApartmentStatus;
import com.houseapp.entity.Announcement;
import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import com.houseapp.entity.BuildingContact;
import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceRequest;
import com.houseapp.entity.MaintenanceStatus;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.Role;
import com.houseapp.entity.User;
import com.houseapp.repository.AnnouncementRepository;
import com.houseapp.repository.ApartmentRepository;
import com.houseapp.repository.BuildingContactRepository;
import com.houseapp.repository.MaintenanceRequestRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
  private final AnnouncementRepository announcementRepository;
  private final BuildingContactRepository buildingContactRepository;
  private final MaintenanceRequestRepository maintenanceRequestRepository;
  private final PasswordEncoder passwordEncoder;

  public PrototypeDataSeeder(
      UserRepository userRepository,
      ApartmentRepository apartmentRepository,
      ResidentProfileRepository residentProfileRepository,
      AnnouncementRepository announcementRepository,
      BuildingContactRepository buildingContactRepository,
      MaintenanceRequestRepository maintenanceRequestRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.apartmentRepository = apartmentRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.announcementRepository = announcementRepository;
    this.buildingContactRepository = buildingContactRepository;
    this.maintenanceRequestRepository = maintenanceRequestRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    User admin = seedUser("Administrator", "admin@house.com", "Admin123!", Role.ADMIN, false);
    User demoResident = seedUser("Demo Resident", "resident@house.com", "Resident123!", Role.RESIDENT, false);

    Apartment apartmentA101 = seedApartment("A", 1, "A-101", "42.50", 1, ApartmentStatus.OCCUPIED);
    seedApartment("A", 1, "A-102", "58.00", 2, ApartmentStatus.VACANT);
    seedApartment("A", 2, "A-201", "64.00", 2, ApartmentStatus.OCCUPIED);
    seedApartment("A", 2, "A-202", "78.50", 3, ApartmentStatus.VACANT);
    seedApartment("A", 3, "A-301", "85.00", 3, ApartmentStatus.MAINTENANCE);
    seedApartment("B", 1, "B-101", "45.00", 1, ApartmentStatus.OCCUPIED);
    seedApartment("B", 1, "B-102", "61.00", 2, ApartmentStatus.VACANT);
    seedApartment("B", 2, "B-201", "73.00", 3, ApartmentStatus.OCCUPIED);
    seedApartment("B", 2, "B-202", "90.00", 4, ApartmentStatus.VACANT);
    seedApartment("C", 1, "C-101", "52.00", 2, ApartmentStatus.VACANT);
    seedApartment("C", 1, "C-102", "59.50", 2, ApartmentStatus.OCCUPIED);
    seedApartment("C", 2, "C-201", "68.00", 3, ApartmentStatus.MAINTENANCE);

    seedResidentProfile(demoResident, apartmentA101, "+380501112233", "Олена Демчук", "+380501112244", null, "Seeded prototype resident profile.");
    seedDemoResident("Olena Kovalenko", "olena.resident@house.com", "TempResident1!", "A-201", "+380671234501");
    seedDemoResident("Andrii Shevchenko", "andrii.resident@house.com", "TempResident1!", "B-101", "+380671234502");
    seedDemoResident("Iryna Melnyk", "iryna.resident@house.com", "TempResident1!", "B-201", "+380671234503");
    seedDemoResident("Taras Bondar", "taras.resident@house.com", "TempResident1!", "C-102", "+380671234504");

    seedAnnouncements(admin);
    seedContacts();
    seedMaintenanceRequests();
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

  private void seedAnnouncements(User admin) {
    Instant now = Instant.now();
    seedAnnouncement(
        admin,
        "Планове відключення води",
        "Scheduled water outage",
        "У середу з 10:00 до 14:00 буде тимчасово припинено водопостачання для планових робіт.",
        "Water supply will be temporarily unavailable on Wednesday from 10:00 to 14:00 for scheduled work.",
        AnnouncementCategory.MAINTENANCE,
        AnnouncementPriority.HIGH,
        AnnouncementStatus.PUBLISHED,
        now.minus(2, ChronoUnit.DAYS),
        now.plus(10, ChronoUnit.DAYS)
    );
    seedAnnouncement(
        admin,
        "Обслуговування ліфта",
        "Elevator maintenance",
        "Ліфт у секції A проходитиме сервісну перевірку у пʼятницю з 09:00 до 12:00.",
        "The elevator in section A will have service inspection on Friday from 09:00 to 12:00.",
        AnnouncementCategory.MAINTENANCE,
        AnnouncementPriority.NORMAL,
        AnnouncementStatus.PUBLISHED,
        now.minus(1, ChronoUnit.DAYS),
        now.plus(14, ChronoUnit.DAYS)
    );
    seedAnnouncement(
        admin,
        "Збори мешканців",
        "Residents meeting",
        "Запрошуємо мешканців на зустріч у холі будинку у суботу о 18:00.",
        "Residents are invited to a meeting in the building lobby on Saturday at 18:00.",
        AnnouncementCategory.EVENT,
        AnnouncementPriority.NORMAL,
        AnnouncementStatus.PUBLISHED,
        now.minus(3, ChronoUnit.HOURS),
        now.plus(20, ChronoUnit.DAYS)
    );
    seedAnnouncement(
        admin,
        "Нагадування про оплату",
        "Payment reminder",
        "Нагадування про необхідність вчасно сплачувати рахунки за обслуговування будинку.",
        "Reminder to pay building service invoices on time.",
        AnnouncementCategory.PAYMENT,
        AnnouncementPriority.LOW,
        AnnouncementStatus.ARCHIVED,
        now.minus(30, ChronoUnit.DAYS),
        now.minus(1, ChronoUnit.DAYS)
    );
    seedAnnouncement(
        admin,
        "Тестова чернетка",
        "Test draft",
        "Чернетка оголошення для перевірки адміністративного інтерфейсу.",
        "Draft announcement for checking the administrative interface.",
        AnnouncementCategory.OTHER,
        AnnouncementPriority.LOW,
        AnnouncementStatus.DRAFT,
        null,
        null
    );
  }

  private void seedAnnouncement(
      User admin,
      String titleUk,
      String titleEn,
      String bodyUk,
      String bodyEn,
      AnnouncementCategory category,
      AnnouncementPriority priority,
      AnnouncementStatus status,
      Instant publishedAt,
      Instant expiresAt
  ) {
    if (announcementRepository.existsByTitleUkIgnoreCase(titleUk)) {
      return;
    }
    Announcement announcement = new Announcement();
    announcement.setTitleUk(titleUk);
    announcement.setTitleEn(titleEn);
    announcement.setBodyUk(bodyUk);
    announcement.setBodyEn(bodyEn);
    announcement.setCategory(category);
    announcement.setPriority(priority);
    announcement.setStatus(status);
    announcement.setPublishedAt(publishedAt);
    announcement.setExpiresAt(expiresAt);
    announcement.setCreatedBy(admin);
    announcementRepository.save(announcement);
  }

  private void seedContacts() {
    seedContact("Керуюча компанія", "Management Company", "Адміністрація", "Administration", "Офіс будинку", "Building office", "+380501110001", "office@house.com", "Пн-Пт 09:00-18:00", "Mon-Fri 09:00-18:00", 10);
    seedContact("Сантехнік", "Plumber", "Технічна підтримка", "Technical support", "Інженерна служба", "Engineering", "+380501110002", null, "Щодня 08:00-20:00", "Daily 08:00-20:00", 20);
    seedContact("Електрик", "Electrician", "Технічна підтримка", "Technical support", "Інженерна служба", "Engineering", "+380501110003", "electric@house.com", "Пн-Сб 08:00-19:00", "Mon-Sat 08:00-19:00", 30);
    seedContact("Охорона", "Security", "Охорона", "Security", "Безпека", "Security", "+380501110004", null, "Цілодобово", "24/7", 40);
    seedContact("Аварійна служба", "Emergency Service", "Аварійна підтримка", "Emergency support", "Міська служба", "Municipal service", null, "emergency@house.com", "Цілодобово", "24/7", 50);
  }

  private void seedContact(
      String nameUk,
      String nameEn,
      String roleUk,
      String roleEn,
      String departmentUk,
      String departmentEn,
      String phone,
      String email,
      String availabilityUk,
      String availabilityEn,
      int sortOrder
  ) {
    if (buildingContactRepository.existsByNameUkIgnoreCase(nameUk)) {
      return;
    }
    BuildingContact contact = new BuildingContact();
    contact.setNameUk(nameUk);
    contact.setNameEn(nameEn);
    contact.setRoleUk(roleUk);
    contact.setRoleEn(roleEn);
    contact.setDepartmentUk(departmentUk);
    contact.setDepartmentEn(departmentEn);
    contact.setPhone(phone);
    contact.setEmail(email);
    contact.setAvailabilityUk(availabilityUk);
    contact.setAvailabilityEn(availabilityEn);
    contact.setSortOrder(sortOrder);
    contact.setActive(true);
    buildingContactRepository.save(contact);
  }

  private void seedMaintenanceRequests() {
    seedMaintenanceRequest("resident@house.com", "Протікання під мийкою", "Потрібна перевірка сифона на кухні.", MaintenanceCategory.PLUMBING, MaintenancePriority.URGENT, MaintenanceStatus.NEW, null, "Resident reports active leak.");
    seedMaintenanceRequest("resident@house.com", "Нестабільний інтернет", "Увечері часто зникає підключення.", MaintenanceCategory.INTERNET, MaintenancePriority.NORMAL, MaintenanceStatus.IN_PROGRESS, "Технік перевіряє обладнання провайдера.", "Coordinate with ISP.");
    seedMaintenanceRequest("olena.resident@house.com", "Не працює розетка", "У кімнаті біля вікна немає живлення.", MaintenanceCategory.ELECTRICITY, MaintenancePriority.HIGH, MaintenanceStatus.NEW, null, "Check breaker and outlet.");
    seedMaintenanceRequest("olena.resident@house.com", "Планове прибирання поверху", "Потрібне додаткове прибирання після ремонту сусідів.", MaintenanceCategory.CLEANING, MaintenancePriority.LOW, MaintenanceStatus.RESOLVED, "Прибирання виконано.", "Resolved by cleaning team.");
    seedMaintenanceRequest("andrii.resident@house.com", "Шум у ліфті", "Ліфт видає скрегіт під час руху.", MaintenanceCategory.ELEVATOR, MaintenancePriority.HIGH, MaintenanceStatus.IN_PROGRESS, "Сервісна компанія вже отримала заявку.", "Elevator vendor notified.");
    seedMaintenanceRequest("iryna.resident@house.com", "Холодні батареї", "У квартирі низька температура, батареї ледь теплі.", MaintenanceCategory.HEATING, MaintenancePriority.HIGH, MaintenanceStatus.WAITING_RESIDENT, "Потрібен доступ до квартири для огляду.", "Waiting for resident availability.");
    seedMaintenanceRequest("taras.resident@house.com", "Освітлення у підʼїзді", "На другому поверсі не працює світильник.", MaintenanceCategory.ELECTRICITY, MaintenancePriority.NORMAL, MaintenanceStatus.RESOLVED, "Лампу замінено.", "Done by electrician.");
    seedMaintenanceRequest("taras.resident@house.com", "Підозрілий доступ у двір", "Ворота не закриваються після 22:00.", MaintenanceCategory.SECURITY, MaintenancePriority.NORMAL, MaintenanceStatus.CANCELLED, "Заявку скасовано після перевірки налаштувань.", "False alarm.");
  }

  private void seedMaintenanceRequest(
      String residentEmail,
      String title,
      String description,
      MaintenanceCategory category,
      MaintenancePriority priority,
      MaintenanceStatus status,
      String adminResponse,
      String internalNotes
  ) {
    User user = userRepository.findByEmail(residentEmail).orElse(null);
    if (user == null) {
      return;
    }
    ResidentProfile profile = residentProfileRepository.findByUserId(user.getId()).orElse(null);
    if (profile == null || maintenanceRequestRepository.existsByTitleIgnoreCaseAndResidentProfileId(title, profile.getId())) {
      return;
    }
    MaintenanceRequest request = new MaintenanceRequest();
    request.setResidentProfile(profile);
    request.setApartment(profile.getApartment());
    request.setTitle(title);
    request.setDescription(description);
    request.setCategory(category);
    request.setPriority(priority);
    request.setStatus(status);
    request.setAdminResponse(adminResponse);
    request.setInternalNotes(internalNotes);
    if (status == MaintenanceStatus.RESOLVED) {
      request.setResolvedAt(Instant.now().minus(2, ChronoUnit.DAYS));
    }
    maintenanceRequestRepository.save(request);
  }
}
