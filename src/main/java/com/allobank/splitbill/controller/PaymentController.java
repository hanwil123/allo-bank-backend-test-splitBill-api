package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.PaymentResponse;
import com.allobank.splitbill.dto.RecordPaymentRequest;
import com.allobank.splitbill.model.Payment;
import com.allobank.splitbill.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(@PathVariable UUID groupId, @Valid @RequestBody RecordPaymentRequest request) {
        Payment payment = paymentService.recordPayment(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping
    public List<PaymentResponse> getPayments(@PathVariable UUID groupId) {
        return paymentService.getPaymentsForGroup(groupId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }
}
