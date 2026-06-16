package com.houseapp.service;

import com.houseapp.dto.request.admin.PaymentRequest;
import com.houseapp.dto.request.admin.PaymentStatusUpdateRequest;
import com.houseapp.dto.response.admin.PaymentAdminResponse;
import com.houseapp.dto.response.resident.PaymentResidentResponse;
import com.houseapp.entity.Payment;
import com.houseapp.entity.PaymentCurrency;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import com.houseapp.entity.ResidentProfile;
import com.houseapp.entity.User;
import com.houseapp.exception.ApiException;
import com.houseapp.mapper.PaymentMapper;
import com.houseapp.repository.PaymentRepository;
import com.houseapp.repository.ResidentProfileRepository;
import com.houseapp.repository.UserRepository;
import com.houseapp.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
  private final PaymentRepository paymentRepository;
  private final ResidentProfileRepository residentProfileRepository;
  private final UserRepository userRepository;
  private final PaymentMapper paymentMapper;

  public PaymentService(
      PaymentRepository paymentRepository,
      ResidentProfileRepository residentProfileRepository,
      UserRepository userRepository,
      PaymentMapper paymentMapper
  ) {
    this.paymentRepository = paymentRepository;
    this.residentProfileRepository = residentProfileRepository;
    this.userRepository = userRepository;
    this.paymentMapper = paymentMapper;
  }

  @Transactional(readOnly = true)
  public List<PaymentResidentResponse> listForResident(
      UserPrincipal principal,
      PaymentStatus status,
      PaymentType type,
      Integer periodYear,
      Integer periodMonth
  ) {
    ResidentProfile profile = findProfileByUser(principal.getId());
    return paymentRepository.searchForResident(profile.getId(), status, type, periodYear, periodMonth).stream()
        .map(paymentMapper::toResidentResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PaymentResidentResponse getForResident(UserPrincipal principal, Long id) {
    ResidentProfile profile = findProfileByUser(principal.getId());
    Payment payment = paymentRepository.findByIdAndResidentProfileId(id, profile.getId())
        .orElseThrow(this::notFound);
    return paymentMapper.toResidentResponse(payment);
  }

  @Transactional(readOnly = true)
  public List<PaymentAdminResponse> listForAdmin(
      PaymentStatus status,
      PaymentType type,
      Long residentId,
      Long apartmentId,
      Integer periodYear,
      Integer periodMonth,
      String search
  ) {
    return paymentRepository.searchForAdmin(status, type, residentId, apartmentId, periodYear, periodMonth, cleanNullable(search)).stream()
        .map(paymentMapper::toAdminResponse)
        .toList();
  }

  @Transactional
  public PaymentAdminResponse create(PaymentRequest request, UserPrincipal principal) {
    Payment payment = new Payment();
    payment.setCreatedBy(findUser(principal.getId()));
    applyRequest(payment, request);
    return paymentMapper.toAdminResponse(paymentRepository.save(payment));
  }

  @Transactional(readOnly = true)
  public PaymentAdminResponse getForAdmin(Long id) {
    return paymentMapper.toAdminResponse(findPayment(id));
  }

  @Transactional
  public PaymentAdminResponse update(Long id, PaymentRequest request) {
    Payment payment = findPayment(id);
    applyRequest(payment, request);
    return paymentMapper.toAdminResponse(paymentRepository.save(payment));
  }

  @Transactional
  public PaymentAdminResponse updateStatus(Long id, PaymentStatusUpdateRequest request) {
    Payment payment = findPayment(id);
    setStatusWithPaidAt(payment, request.status());
    return paymentMapper.toAdminResponse(paymentRepository.save(payment));
  }

  @Transactional
  public void cancel(Long id) {
    Payment payment = findPayment(id);
    setStatusWithPaidAt(payment, PaymentStatus.CANCELLED);
    paymentRepository.save(payment);
  }

  private void applyRequest(Payment payment, PaymentRequest request) {
    ResidentProfile profile = residentProfileRepository.findById(request.residentProfileId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resident profile not found"));
    payment.setResidentProfile(profile);
    payment.setApartment(profile.getApartment());
    payment.setType(request.type());
    payment.setAmountMinor(request.amountMinor());
    payment.setCurrency(request.currency() == null ? PaymentCurrency.UAH : request.currency());
    payment.setPeriodYear(request.periodYear());
    payment.setPeriodMonth(request.periodMonth());
    payment.setTitleUk(clean(request.titleUk()));
    payment.setTitleEn(cleanNullable(request.titleEn()));
    payment.setDescriptionUk(cleanNullable(request.descriptionUk()));
    payment.setDescriptionEn(cleanNullable(request.descriptionEn()));
    payment.setDueDate(request.dueDate());
    setStatusWithPaidAt(payment, request.status() == null ? PaymentStatus.PENDING : request.status());
  }

  private void setStatusWithPaidAt(Payment payment, PaymentStatus status) {
    payment.setStatus(status);
    if (status == PaymentStatus.PAID && payment.getPaidAt() == null) {
      payment.setPaidAt(Instant.now());
    }
    if (status != PaymentStatus.PAID) {
      payment.setPaidAt(null);
    }
  }

  private Payment findPayment(Long id) {
    return paymentRepository.findById(id).orElseThrow(this::notFound);
  }

  private ResidentProfile findProfileByUser(Long userId) {
    return residentProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resident profile not found"));
  }

  private User findUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Payment not found");
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private String cleanNullable(String value) {
    String cleaned = clean(value);
    return cleaned.isEmpty() ? null : cleaned;
  }
}
