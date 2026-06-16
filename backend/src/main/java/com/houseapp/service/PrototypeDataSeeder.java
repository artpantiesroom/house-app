package com.houseapp.service;

import com.houseapp.entity.Apartment;
import com.houseapp.entity.ApartmentStatus;
import com.houseapp.entity.AuditAction;
import com.houseapp.entity.AuditEntityType;
import com.houseapp.entity.AuditLog;
import com.houseapp.entity.Announcement;
import com.houseapp.entity.AnnouncementCategory;
import com.houseapp.entity.AnnouncementPriority;
import com.houseapp.entity.AnnouncementStatus;
import com.houseapp.entity.BuildingContact;
import com.houseapp.entity.MaintenanceCategory;
import com.houseapp.entity.MaintenancePriority;
import com.houseapp.entity.MaintenanceRequest;
import com.houseapp.entity.MaintenanceStatus;
import com.houseapp.entity.Payment;
import com.houseapp.entity.PaymentCurrency;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.Role;
import com.houseapp.entity.SecurityIncident;
import com.houseapp.entity.SecurityIncidentCategory;
import com.houseapp.entity.SecurityIncidentSeverity;
import com.houseapp.entity.SecurityIncidentStatus;
import com.houseapp.entity.User;
import com.houseapp.repository.AuditLogRepository;
import com.houseapp.repository.AnnouncementRepository;
import com.houseapp.repository.ApartmentRepository;
import com.houseapp.repository.BuildingContactRepository;
import com.houseapp.repository.MaintenanceRequestRepository;
import com.houseapp.repository.PaymentRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.repository.SecurityIncidentRepository;
import com.houseapp.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
  private final PaymentRepository paymentRepository;
  private final AuditLogRepository auditLogRepository;
  private final SecurityIncidentRepository securityIncidentRepository;
  private final PasswordEncoder passwordEncoder;

  public PrototypeDataSeeder(
      UserRepository userRepository,
      ApartmentRepository apartmentRepository,
      ResidentProfileRepository residentProfileRepository,
      AnnouncementRepository announcementRepository,
      BuildingContactRepository buildingContactRepository,
      MaintenanceRequestRepository maintenanceRequestRepository,
      PaymentRepository paymentRepository,
      AuditLogRepository auditLogRepository,
      SecurityIncidentRepository securityIncidentRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.apartmentRepository = apartmentRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.announcementRepository = announcementRepository;
    this.buildingContactRepository = buildingContactRepository;
    this.maintenanceRequestRepository = maintenanceRequestRepository;
    this.paymentRepository = paymentRepository;
    this.auditLogRepository = auditLogRepository;
    this.securityIncidentRepository = securityIncidentRepository;
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
    seedPayments(admin);
    seedAuditLogs(admin, demoResident);
    seedSecurityIncidents(admin, demoResident);
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

  private void seedPayments(User admin) {
    seedPayment("resident@house.com", PaymentType.UTILITIES, PaymentStatus.PENDING, 125000L, 2026, 6, "Комунальні послуги червень 2026", "Utilities June 2026", "Вода, електрика та загальнобудинкові витрати.", "Water, electricity, and shared building costs.", LocalDate.of(2026, 6, 25), admin);
    seedPayment("resident@house.com", PaymentType.MAINTENANCE, PaymentStatus.PAID, 65000L, 2026, 6, "Внесок на обслуговування червень 2026", "Maintenance fee June 2026", "Щомісячне обслуговування будинку.", "Monthly building maintenance.", LocalDate.of(2026, 6, 15), admin);
    seedPayment("resident@house.com", PaymentType.SECURITY, PaymentStatus.OVERDUE, 30000L, 2026, 5, "Охорона травень 2026", "Security fee May 2026", "Послуги охорони території.", "Building security services.", LocalDate.of(2026, 5, 20), admin);
    seedPayment("olena.resident@house.com", PaymentType.RENT, PaymentStatus.PENDING, 850000L, 2026, 6, "Оренда червень 2026", "Rent June 2026", "Орендна плата за квартиру.", "Apartment rent.", LocalDate.of(2026, 6, 10), admin);
    seedPayment("olena.resident@house.com", PaymentType.UTILITIES, PaymentStatus.PAID, 118500L, 2026, 5, "Комунальні послуги травень 2026", "Utilities May 2026", "Комунальні послуги за травень.", "Utilities for May.", LocalDate.of(2026, 5, 25), admin);
    seedPayment("andrii.resident@house.com", PaymentType.PARKING, PaymentStatus.PENDING, 120000L, 2026, 6, "Паркування червень 2026", "Parking June 2026", "Паркомісце у підземному паркінгу.", "Underground parking space.", LocalDate.of(2026, 6, 18), admin);
    seedPayment("andrii.resident@house.com", PaymentType.MAINTENANCE, PaymentStatus.OVERDUE, 65000L, 2026, 5, "Внесок на обслуговування травень 2026", "Maintenance fee May 2026", "Щомісячне обслуговування будинку.", "Monthly building maintenance.", LocalDate.of(2026, 5, 15), admin);
    seedPayment("iryna.resident@house.com", PaymentType.SECURITY, PaymentStatus.PAID, 30000L, 2026, 6, "Охорона червень 2026", "Security fee June 2026", "Послуги охорони території.", "Building security services.", LocalDate.of(2026, 6, 20), admin);
    seedPayment("iryna.resident@house.com", PaymentType.UTILITIES, PaymentStatus.PENDING, 142000L, 2026, 6, "Комунальні послуги червень 2026", "Utilities June 2026", "Комунальні послуги за червень.", "Utilities for June.", LocalDate.of(2026, 6, 25), admin);
    seedPayment("taras.resident@house.com", PaymentType.OTHER, PaymentStatus.CANCELLED, 45000L, 2026, 4, "Разовий адміністративний платіж", "One-time administrative fee", "Скасований тестовий запис.", "Cancelled test record.", LocalDate.of(2026, 4, 30), admin);
    seedPayment("taras.resident@house.com", PaymentType.RENT, PaymentStatus.PAID, 780000L, 2026, 5, "Оренда травень 2026", "Rent May 2026", "Орендна плата за квартиру.", "Apartment rent.", LocalDate.of(2026, 5, 10), admin);
    seedPayment("taras.resident@house.com", PaymentType.MAINTENANCE, PaymentStatus.PENDING, 65000L, 2026, 7, "Внесок на обслуговування липень 2026", "Maintenance fee July 2026", "Щомісячне обслуговування будинку.", "Monthly building maintenance.", LocalDate.of(2026, 7, 15), admin);
  }

  private void seedPayment(
      String residentEmail,
      PaymentType type,
      PaymentStatus status,
      Long amountMinor,
      Integer periodYear,
      Integer periodMonth,
      String titleUk,
      String titleEn,
      String descriptionUk,
      String descriptionEn,
      LocalDate dueDate,
      User admin
  ) {
    User user = userRepository.findByEmail(residentEmail).orElse(null);
    if (user == null) {
      return;
    }
    ResidentProfile profile = residentProfileRepository.findByUserId(user.getId()).orElse(null);
    if (profile == null || paymentRepository.existsByResidentProfileIdAndTypeAndPeriodYearAndPeriodMonthAndTitleUkIgnoreCase(profile.getId(), type, periodYear, periodMonth, titleUk)) {
      return;
    }
    Payment payment = new Payment();
    payment.setResidentProfile(profile);
    payment.setApartment(profile.getApartment());
    payment.setType(type);
    payment.setStatus(status);
    payment.setAmountMinor(amountMinor);
    payment.setCurrency(PaymentCurrency.UAH);
    payment.setPeriodYear(periodYear);
    payment.setPeriodMonth(periodMonth);
    payment.setTitleUk(titleUk);
    payment.setTitleEn(titleEn);
    payment.setDescriptionUk(descriptionUk);
    payment.setDescriptionEn(descriptionEn);
    payment.setDueDate(dueDate);
    payment.setCreatedBy(admin);
    if (status == PaymentStatus.PAID) {
      payment.setPaidAt(Instant.now().minus(1, ChronoUnit.DAYS));
    }
    paymentRepository.save(payment);
  }

  private void seedAuditLogs(User admin, User resident) {
    seedAuditLog(admin, AuditAction.LOGIN_SUCCESS, AuditEntityType.AUTH, admin.getId(), "Administrator demo login succeeded", "{\"source\":\"seed\"}", Instant.now().minus(12, ChronoUnit.DAYS));
    seedAuditLog(resident, AuditAction.LOGIN_SUCCESS, AuditEntityType.AUTH, resident.getId(), "Resident demo login succeeded", "{\"source\":\"seed\"}", Instant.now().minus(11, ChronoUnit.DAYS));
    seedAuditLog(null, AuditAction.LOGIN_FAILED, AuditEntityType.AUTH, null, "Failed login for unknown user", "{\"email\":\"unknown@example.com\"}", Instant.now().minus(10, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.RESIDENT_CREATED, AuditEntityType.RESIDENT, 1L, "Resident profile created from seed", "{\"source\":\"seed\"}", Instant.now().minus(9, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.RESIDENT_UPDATED, AuditEntityType.RESIDENT, 1L, "Resident profile updated from seed", "{\"source\":\"seed\"}", Instant.now().minus(8, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.APARTMENT_UPDATED, AuditEntityType.APARTMENT, 1L, "Apartment occupancy updated", "{\"apartment\":\"A-101\"}", Instant.now().minus(7, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.ANNOUNCEMENT_PUBLISHED, AuditEntityType.ANNOUNCEMENT, 1L, "Water outage announcement published", "{\"classification\":\"Public\"}", Instant.now().minus(6, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.CONTACT_UPDATED, AuditEntityType.CONTACT, 1L, "Management company contact updated", "{\"source\":\"seed\"}", Instant.now().minus(5, ChronoUnit.DAYS));
    seedAuditLog(resident, AuditAction.MAINTENANCE_CREATED, AuditEntityType.MAINTENANCE_REQUEST, 1L, "Maintenance request created by resident", "{\"category\":\"PLUMBING\"}", Instant.now().minus(4, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.MAINTENANCE_UPDATED, AuditEntityType.MAINTENANCE_REQUEST, 1L, "Maintenance request moved to in progress", "{\"status\":\"IN_PROGRESS\"}", Instant.now().minus(3, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.PAYMENT_CREATED, AuditEntityType.PAYMENT, 1L, "Payment record created", "{\"amountMinor\":125000}", Instant.now().minus(2, ChronoUnit.DAYS));
    seedAuditLog(admin, AuditAction.PAYMENT_STATUS_CHANGED, AuditEntityType.PAYMENT, 2L, "Payment status changed to PAID", "{\"status\":\"PAID\"}", Instant.now().minus(36, ChronoUnit.HOURS));
    seedAuditLog(admin, AuditAction.PAYMENT_CANCELLED, AuditEntityType.PAYMENT, 10L, "Payment cancelled", "{\"status\":\"CANCELLED\"}", Instant.now().minus(24, ChronoUnit.HOURS));
    seedAuditLog(admin, AuditAction.SECURITY_INCIDENT_CREATED, AuditEntityType.SECURITY_INCIDENT, 1L, "Security incident created from seed", "{\"severity\":\"HIGH\"}", Instant.now().minus(18, ChronoUnit.HOURS));
    seedAuditLog(null, AuditAction.SECURITY_INCIDENT_RESOLVED, AuditEntityType.SYSTEM, null, "System audit seed event", "{\"source\":\"system\"}", Instant.now().minus(12, ChronoUnit.HOURS));
  }

  private void seedAuditLog(
      User actor,
      AuditAction action,
      AuditEntityType entityType,
      Long entityId,
      String summary,
      String metadataJson,
      Instant createdAt
  ) {
    if (auditLogRepository.existsByActionAndEntityTypeAndEntityIdAndSummary(action, entityType, entityId, summary)) {
      return;
    }
    AuditLog auditLog = new AuditLog();
    auditLog.setActorUser(actor);
    auditLog.setActorEmail(actor == null ? "system" : actor.getEmail());
    auditLog.setActorRole(actor == null ? "SYSTEM" : actor.getRole().name());
    auditLog.setAction(action);
    auditLog.setEntityType(entityType);
    auditLog.setEntityId(entityId);
    auditLog.setSummary(summary);
    auditLog.setMetadataJson(metadataJson);
    auditLog.setIpAddress("127.0.0.1");
    auditLog.setUserAgent("PrototypeDataSeeder");
    auditLog.setCreatedAt(createdAt);
    auditLogRepository.save(auditLog);
  }

  private void seedSecurityIncidents(User admin, User resident) {
    AuditLog relatedLog = auditLogRepository.search(AuditAction.LOGIN_FAILED, AuditEntityType.AUTH, null, null, null, null, null)
        .stream()
        .findFirst()
        .orElse(null);
    seedSecurityIncident("Repeated failed login attempts", "Several failed authentication attempts were observed for a non-existing account.", SecurityIncidentSeverity.HIGH, SecurityIncidentStatus.OPEN, SecurityIncidentCategory.AUTHENTICATION, null, admin, relatedLog, null);
    seedSecurityIncident("Resident route authorization review", "A resident attempted to open an administrator route during prototype testing.", SecurityIncidentSeverity.MEDIUM, SecurityIncidentStatus.INVESTIGATING, SecurityIncidentCategory.AUTHORIZATION, resident, admin, null, null);
    seedSecurityIncident("Payment record visibility check", "Administrator verified that resident payment records are isolated by owner.", SecurityIncidentSeverity.LOW, SecurityIncidentStatus.RESOLVED, SecurityIncidentCategory.PAYMENT, admin, admin, null, "Visibility rule confirmed.");
    seedSecurityIncident("Suspicious maintenance note", "A maintenance request looked suspicious but was confirmed as a training data artifact.", SecurityIncidentSeverity.LOW, SecurityIncidentStatus.FALSE_POSITIVE, SecurityIncidentCategory.MAINTENANCE, null, admin, null, "False positive in seeded prototype data.");
    seedSecurityIncident("Critical system configuration drill", "Prototype drill for reviewing JWT and CORS configuration after tunnel exposure.", SecurityIncidentSeverity.CRITICAL, SecurityIncidentStatus.INVESTIGATING, SecurityIncidentCategory.SYSTEM, admin, admin, null, null);
  }

  private void seedSecurityIncident(
      String title,
      String description,
      SecurityIncidentSeverity severity,
      SecurityIncidentStatus status,
      SecurityIncidentCategory category,
      User reportedBy,
      User assignedTo,
      AuditLog relatedAuditLog,
      String resolutionNotes
  ) {
    if (securityIncidentRepository.existsByTitleIgnoreCase(title)) {
      return;
    }
    SecurityIncident incident = new SecurityIncident();
    incident.setTitle(title);
    incident.setDescription(description);
    incident.setSeverity(severity);
    incident.setStatus(status);
    incident.setCategory(category);
    incident.setReportedBy(reportedBy);
    incident.setAssignedTo(assignedTo);
    incident.setRelatedAuditLog(relatedAuditLog);
    incident.setResolutionNotes(resolutionNotes);
    if (status == SecurityIncidentStatus.RESOLVED || status == SecurityIncidentStatus.FALSE_POSITIVE) {
      incident.setResolvedAt(Instant.now().minus(6, ChronoUnit.HOURS));
    }
    securityIncidentRepository.save(incident);
  }
}
