package com.houseapp.controller;

import com.houseapp.dto.request.admin.PaymentRequest;
import com.houseapp.dto.request.admin.PaymentStatusUpdateRequest;
import com.houseapp.dto.response.admin.PaymentAdminResponse;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {
  private final PaymentService paymentService;

  public AdminPaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping
  public List<PaymentAdminResponse> list(
      @RequestParam(required = false) PaymentStatus status,
      @RequestParam(required = false) PaymentType type,
      @RequestParam(required = false) Long residentId,
      @RequestParam(required = false) Long apartmentId,
      @RequestParam(required = false) Integer periodYear,
      @RequestParam(required = false) Integer periodMonth,
      @RequestParam(required = false) String search
  ) {
    return paymentService.listForAdmin(status, type, residentId, apartmentId, periodYear, periodMonth, search);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentAdminResponse create(
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return paymentService.create(request, principal, servletRequest);
  }

  @GetMapping("/{id}")
  public PaymentAdminResponse get(@PathVariable Long id) {
    return paymentService.getForAdmin(id);
  }

  @PutMapping("/{id}")
  public PaymentAdminResponse update(
      @PathVariable Long id,
      @Valid @RequestBody PaymentRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return paymentService.update(id, request, principal, servletRequest);
  }

  @PatchMapping("/{id}/status")
  public PaymentAdminResponse updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody PaymentStatusUpdateRequest request,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    return paymentService.updateStatus(id, request, principal, servletRequest);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancel(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal principal,
      HttpServletRequest servletRequest
  ) {
    paymentService.cancel(id, principal, servletRequest);
  }
}
