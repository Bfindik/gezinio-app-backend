package com.example.agentapp.admin.service;

import com.example.agentapp.auth.model.User;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.payment.repository.RefundRepository;
import com.example.agentapp.reservation.model.Reservation;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock RefundRepository refundRepository;

    @InjectMocks AdminReservationService service;

    private Reservation pendingReservation;
    private Reservation cancelledReservation;
    private Reservation completedReservation;

    @BeforeEach
    void setUp() {
        User user = new User("tourist", "t@mail.com", "hash");
        user.setId(1L);

        pendingReservation = buildReservation(user, ReservationStatus.PENDING);
        cancelledReservation = buildReservation(user, ReservationStatus.CANCELLED);
        completedReservation = buildReservation(user, ReservationStatus.COMPLETED);
    }

    // ─── confirmReservation ───────────────────────────────────────────────────

    @Test
    void confirmReservation_whenAlreadyCancelled_throwsBadRequest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(cancelledReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.confirmReservation(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void confirmReservation_whenCompleted_throwsBadRequest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(completedReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.confirmReservation(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void confirmReservation_success_setsConfirmed() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(paymentRepository.sumCompletedPaymentsByReservationId(any())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.sumCompletedRefundsByReservationId(any())).thenReturn(BigDecimal.ZERO);
        when(reservationRepository.save(any())).thenReturn(pendingReservation);

        service.confirmReservation(1L);

        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.getReservationStatus());
        verify(reservationRepository).save(pendingReservation);
    }

    // ─── cancelReservation ────────────────────────────────────────────────────

    @Test
    void cancelReservation_whenAlreadyCancelled_throwsBadRequest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(cancelledReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cancelReservation(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cancelReservation_whenCompleted_throwsBadRequest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(completedReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cancelReservation(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cancelReservation_success_setsCancelled() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(paymentRepository.sumCompletedPaymentsByReservationId(any())).thenReturn(BigDecimal.ZERO);
        when(refundRepository.sumCompletedRefundsByReservationId(any())).thenReturn(BigDecimal.ZERO);
        when(reservationRepository.save(any())).thenReturn(pendingReservation);

        service.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, pendingReservation.getReservationStatus());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Reservation buildReservation(User owner, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setUser(owner);
        r.setReservationType(ReservationType.TOUR);
        r.setReservationStatus(status);
        r.setNumberOfParticipants(2);
        r.setTotalPrice(new BigDecimal("1000.00"));
        r.setCurrency("TRY");
        return r;
    }
}
