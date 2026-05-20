package com.example.agentapp.payment.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.payment.dto.*;
import com.example.agentapp.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
@Tag(name = "Payments", description = "Reservation payment and refund management (ADMIN/AGENT only)")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Record a new payment against a reservation")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by its ID")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "List all payments for a given reservation")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentsByReservation(reservationId));
    }

    @GetMapping("/reservation/{reservationId}/summary")
    @Operation(summary = "Get the aggregated payment summary for a reservation")
    public ResponseEntity<ReservationPaymentSummaryDTO> getPaymentSummary(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(reservationId));
    }

    @PostMapping("/refund")
    @Operation(summary = "Create a refund for an existing payment")
    public ResponseEntity<RefundDTO> createRefund(
            @Valid @RequestBody RefundCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createRefund(request, currentUser.getId()));
    }

    @GetMapping("/refund/{id}")
    @Operation(summary = "Get a refund by its ID")
    public ResponseEntity<RefundDTO> getRefundById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getRefundById(id));
    }

    @GetMapping("/{paymentId}/refunds")
    @Operation(summary = "List all refunds associated with a payment")
    public ResponseEntity<List<RefundDTO>> getRefundsByPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getRefundsByPayment(paymentId));
    }
}