package com.example.gezinio.payment.service;

import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.payment.dto.PaymentCreateRequest;
import com.example.gezinio.payment.dto.RefundCreateRequest;
import com.example.gezinio.payment.model.*;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.payment.repository.RefundRepository;
import com.example.gezinio.reservation.model.Reservation;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock RefundRepository refundRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks PaymentService service;

    private Reservation activeReservation;

    @BeforeEach
    void setUp() {
        activeReservation = mock(Reservation.class);
        when(activeReservation.getId()).thenReturn(1L);
        when(activeReservation.getReservationStatus()).thenReturn(ReservationStatus.PENDING);
        when(activeReservation.getTotalPrice()).thenReturn(new BigDecimal("1000.00"));
        when(activeReservation.getCurrency()).thenReturn("TRY");
        when(activeReservation.getUser()).thenReturn(mock(com.example.gezinio.auth.model.User.class));
    }

    // ─── createPayment ────────────────────────────────────────────────────────

    @Test
    void createPayment_whenCancelledReservation_throwsBadRequest() {
        when(activeReservation.getReservationStatus()).thenReturn(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPayment(paymentRequest("TRY", "500.00")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createPayment_whenDuplicateTransactionRef_throwsConflict() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));
        when(paymentRepository.existsByTransactionReference("TXN-001")).thenReturn(true);

        PaymentCreateRequest req = paymentRequest("TRY", "500.00");
        req.setTransactionReference("TXN-001");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPayment(req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createPayment_whenOverpayment_throwsBadRequest() {
        // Already paid 800, total is 1000, remaining 200 — trying to pay 300
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));
        when(paymentRepository.sumCompletedPaymentsByReservationId(1L))
                .thenReturn(new BigDecimal("800.00"));

        PaymentCreateRequest req = paymentRequest("TRY", "300.00");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPayment(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("exceeds remaining balance"));
    }

    @Test
    void createPayment_success_savesAndPublishesEvent() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation));
        when(paymentRepository.sumCompletedPaymentsByReservationId(1L))
                .thenReturn(BigDecimal.ZERO);
        when(refundRepository.sumCompletedRefundsByReservationId(1L))
                .thenReturn(BigDecimal.ZERO);

        Payment saved = new Payment();
        saved.setAmount(new BigDecimal("500.00"));
        saved.setCurrency("TRY");
        saved.setExchangeRate(BigDecimal.ONE);
        saved.setAmountInBaseCurrency(new BigDecimal("500.00"));
        saved.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        saved.setPaymentStatus(PaymentStatus.COMPLETED);
        saved.setReservation(activeReservation);

        when(paymentRepository.save(any())).thenReturn(saved);

        service.createPayment(paymentRequest("TRY", "500.00"));

        verify(paymentRepository).save(any(Payment.class));
        verify(eventPublisher).publishEvent(any(AppNotificationEvent.class));
    }

    // ─── createRefund ─────────────────────────────────────────────────────────

    @Test
    void createRefund_whenPaymentFailed_throwsBadRequest() {
        Payment failed = new Payment();
        failed.setPaymentStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById(5L)).thenReturn(Optional.of(failed));

        RefundCreateRequest req = refundRequest(5L, "100.00", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createRefund(req, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createRefund_whenExceedsMaxRefundable_throwsBadRequest() {
        // Payment of 100 TRY, no prior refunds, cancellation fee 10 → max refundable = 90
        Payment payment = new Payment();
        payment.setAmountInBaseCurrency(new BigDecimal("100.00"));
        payment.setExchangeRate(BigDecimal.ONE);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setReservation(activeReservation);
        // refunds list is empty → getTotalRefundedAmount() == 0

        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        RefundCreateRequest req = refundRequest(5L, "95.00", new BigDecimal("10.00"));
        // refundAmountInBase = 95 * 1 = 95 > maxRefundable(90) → BAD_REQUEST

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createRefund(req, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createRefund_success_savesAndPublishesEvent() {
        Payment payment = new Payment();
        payment.setAmountInBaseCurrency(new BigDecimal("100.00"));
        payment.setExchangeRate(BigDecimal.ONE);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setReservation(activeReservation);

        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));

        Refund saved = new Refund();
        saved.setRefundAmountInBaseCurrency(new BigDecimal("50.00"));
        saved.setRefundStatus(RefundStatus.COMPLETED);
        saved.setPayment(payment);

        when(refundRepository.save(any())).thenReturn(saved);
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentRepository.sumCompletedPaymentsByReservationId(any()))
                .thenReturn(new BigDecimal("100.00"));
        when(refundRepository.sumCompletedRefundsByReservationId(any()))
                .thenReturn(new BigDecimal("50.00"));

        service.createRefund(refundRequest(5L, "50.00", null), 1L);

        verify(refundRepository).save(any(Refund.class));
        verify(eventPublisher).publishEvent(any(AppNotificationEvent.class));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private PaymentCreateRequest paymentRequest(String currency, String amount) {
        PaymentCreateRequest req = new PaymentCreateRequest();
        req.setReservationId(1L);
        req.setAmount(new BigDecimal(amount));
        req.setCurrency(currency);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setExchangeRate(BigDecimal.ONE);
        return req;
    }

    private RefundCreateRequest refundRequest(Long paymentId, String amount, BigDecimal fee) {
        RefundCreateRequest req = new RefundCreateRequest();
        req.setPaymentId(paymentId);
        req.setRefundAmount(new BigDecimal(amount));
        req.setRefundCurrency("TRY");
        req.setCancellationFee(fee != null ? fee : BigDecimal.ZERO);
        req.setRefundReason(RefundReason.CUSTOMER_CANCELLATION);
        return req;
    }
}
