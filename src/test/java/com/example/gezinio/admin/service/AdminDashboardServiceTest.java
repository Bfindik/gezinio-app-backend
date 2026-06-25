package com.example.gezinio.admin.service;

import com.example.gezinio.admin.dto.DashboardStatsDTO;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.notification.model.NotificationLog;
import com.example.gezinio.notification.model.NotificationStatus;
import com.example.gezinio.notification.model.NotificationType;
import com.example.gezinio.notification.repository.NotificationLogRepository;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.payment.repository.RefundRepository;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDashboardServiceTest {

    @Mock UserRepository userRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock TourRepository tourRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock RefundRepository refundRepository;
    @Mock NotificationLogRepository notificationLogRepository;

    @InjectMocks AdminDashboardService service;

    @Test
    void getStats_aggregatesAllMetricsCorrectly() {
        // Users
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByEnabled(true)).thenReturn(45L);
        when(userRepository.findByAccountNonLocked(false)).thenReturn(List.of());
        when(userRepository.findByCreatedAtAfter(any())).thenReturn(List.of());

        // Reservations
        when(reservationRepository.count()).thenReturn(100L);
        when(reservationRepository.countByReservationStatus(ReservationStatus.PENDING)).thenReturn(20L);
        when(reservationRepository.countByReservationStatus(ReservationStatus.CONFIRMED)).thenReturn(60L);
        when(reservationRepository.countByReservationStatus(ReservationStatus.CANCELLED)).thenReturn(15L);
        when(reservationRepository.countByReservationStatus(ReservationStatus.COMPLETED)).thenReturn(5L);
        when(reservationRepository.countCreatedAfter(any())).thenReturn(10L);

        // Revenue
        when(paymentRepository.sumAllCompletedPayments()).thenReturn(new BigDecimal("50000.00"));
        when(paymentRepository.sumCompletedPaymentsSince(any())).thenReturn(new BigDecimal("5000.00"));
        when(refundRepository.sumAllCompletedRefunds()).thenReturn(new BigDecimal("2000.00"));

        // Tours
        Tour active = buildTour(1L, TourStatus.ACTIVE);
        Tour draft = buildTour(2L, TourStatus.DRAFT);
        when(tourRepository.findAll()).thenReturn(List.of(active, draft));
        when(reservationRepository.countByTourId(any())).thenReturn(0L);

        // Notifications
        NotificationLog sentEmail = new NotificationLog();
        sentEmail.setType(NotificationType.EMAIL);
        NotificationLog failedEmail = new NotificationLog();
        failedEmail.setType(NotificationType.EMAIL);

        when(notificationLogRepository.findByStatus(NotificationStatus.SENT)).thenReturn(List.of(sentEmail));
        when(notificationLogRepository.findByStatus(NotificationStatus.FAILED)).thenReturn(List.of(failedEmail));

        DashboardStatsDTO stats = service.getStats();

        assertEquals(50L, stats.getTotalUsers());
        assertEquals(45L, stats.getActiveUsers());
        assertEquals(100L, stats.getTotalReservations());
        assertEquals(20L, stats.getPendingReservations());
        assertEquals(60L, stats.getConfirmedReservations());
        assertEquals(new BigDecimal("50000.00"), stats.getTotalRevenue());
        assertEquals(new BigDecimal("48000.00"), stats.getNetRevenue()); // 50000 - 2000
        assertEquals(2L, stats.getTotalTours());
        assertEquals(1L, stats.getActiveTours());
        assertEquals(1L, stats.getDraftTours());
        assertEquals(1L, stats.getTotalEmailsSent());
        assertEquals(1L, stats.getFailedEmails());
    }

    @Test
    void getStats_netRevenue_isRevenueMinusRefunds() {
        stubMinimalDefaults();
        when(paymentRepository.sumAllCompletedPayments()).thenReturn(new BigDecimal("10000.00"));
        when(refundRepository.sumAllCompletedRefunds()).thenReturn(new BigDecimal("1500.00"));

        DashboardStatsDTO stats = service.getStats();

        assertEquals(new BigDecimal("8500.00"), stats.getNetRevenue());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Tour buildTour(Long id, TourStatus status) {
        Tour t = new Tour("Tour " + id, "Dest", new BigDecimal("100.00"));
        t.setId(id);
        t.setTourStatus(status);
        t.setTourType(TourType.GROUP);
        t.setDurationDays(1);
        return t;
    }

    private void stubMinimalDefaults() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByEnabled(true)).thenReturn(0L);
        when(userRepository.findByAccountNonLocked(false)).thenReturn(List.of());
        when(userRepository.findByCreatedAtAfter(any())).thenReturn(List.of());
        when(reservationRepository.count()).thenReturn(0L);
        when(reservationRepository.countByReservationStatus(any())).thenReturn(0L);
        when(reservationRepository.countCreatedAfter(any())).thenReturn(0L);
        when(paymentRepository.sumCompletedPaymentsSince(any())).thenReturn(BigDecimal.ZERO);
        when(tourRepository.findAll()).thenReturn(List.of());
        when(notificationLogRepository.findByStatus(any())).thenReturn(List.of());
    }
}
