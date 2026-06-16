package com.houseapp.controller;

import com.houseapp.dto.response.resident.PaymentResidentResponse;
import com.houseapp.entity.PaymentStatus;
import com.houseapp.entity.PaymentType;
import com.houseapp.security.UserPrincipal;
import com.houseapp.service.PaymentService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident/payments")
public class ResidentPaymentController {
  private final PaymentService paymentService;

  public ResidentPaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping
  public List<PaymentResidentResponse> list(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(required = false) PaymentStatus status,
      @RequestParam(required = false) PaymentType type,
      @RequestParam(required = false) Integer periodYear,
      @RequestParam(required = false) Integer periodMonth
  ) {
    return paymentService.listForResident(principal, status, type, periodYear, periodMonth);
  }

  @GetMapping("/{id}")
  public PaymentResidentResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
    return paymentService.getForResident(principal, id);
  }
}
