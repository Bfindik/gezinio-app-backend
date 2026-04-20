package com.example.agentapp.admin.service;

import com.example.agentapp.admin.dto.AdminTourStatsDTO;
import com.example.agentapp.excursion.model.Tour;
import com.example.agentapp.excursion.model.TourStatus;
import com.example.agentapp.excursion.model.TourType;
import com.example.agentapp.excursion.repository.TourRepository;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminTourServiceTest {

    @Mock TourRepository tourRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks AdminTourService service;

    private Tour tourA;
    private Tour tourB;

    @BeforeEach
    void setUp() {
        tourA = new Tour("Cappadocia Tour", "Cappadocia", new BigDecimal("500.00"));
        tourA.setId(1L);
        tourA.setTourStatus(TourStatus.ACTIVE);
        tourA.setTourType(TourType.GROUP);
        tourA.setDurationDays(3);
        tourA.setCurrency("TRY");

        tourB = new Tour("Istanbul Tour", "Istanbul", new BigDecimal("300.00"));
        tourB.setId(2L);
        tourB.setTourStatus(TourStatus.DRAFT);
        tourB.setTourType(TourType.PRIVATE);
        tourB.setDurationDays(2);
        tourB.setCurrency("TRY");
    }

    // ─── getAllTourStats ───────────────────────────────────────────────────────

    @Test
    void getAllTourStats_populatesStatsCorrectly() {
        when(tourRepository.findAll()).thenReturn(List.of(tourA));
        when(reservationRepository.countByTourId(1L)).thenReturn(10L);
        when(reservationRepository.countByTourIdAndReservationStatus(1L, ReservationStatus.PENDING)).thenReturn(3L);
        when(reservationRepository.countByTourIdAndReservationStatus(1L, ReservationStatus.CONFIRMED)).thenReturn(5L);
        when(reservationRepository.countByTourIdAndReservationStatus(1L, ReservationStatus.CANCELLED)).thenReturn(2L);
        when(paymentRepository.sumCompletedPaymentsByTourId(1L)).thenReturn(new BigDecimal("5000.00"));

        List<AdminTourStatsDTO> result = service.getAllTourStats();

        assertEquals(1, result.size());
        AdminTourStatsDTO dto = result.get(0);
        assertEquals("Cappadocia Tour", dto.getName());
        assertEquals(10L, dto.getTotalBookings());
        assertEquals(3L, dto.getPendingBookings());
        assertEquals(5L, dto.getConfirmedBookings());
        assertEquals(2L, dto.getCancelledBookings());
        assertEquals(new BigDecimal("5000.00"), dto.getTotalRevenue());
    }

    @Test
    void getAllTourStats_sortedByTotalBookingsDescending() {
        when(tourRepository.findAll()).thenReturn(List.of(tourA, tourB));

        // tourA has 2 bookings, tourB has 5 — tourB should come first
        when(reservationRepository.countByTourId(1L)).thenReturn(2L);
        when(reservationRepository.countByTourId(2L)).thenReturn(5L);
        when(reservationRepository.countByTourIdAndReservationStatus(any(), any())).thenReturn(0L);
        when(paymentRepository.sumCompletedPaymentsByTourId(any())).thenReturn(BigDecimal.ZERO);

        List<AdminTourStatsDTO> result = service.getAllTourStats();

        assertEquals("Istanbul Tour", result.get(0).getName());
        assertEquals("Cappadocia Tour", result.get(1).getName());
    }

    @Test
    void getAllTourStats_whenNoTours_returnsEmptyList() {
        when(tourRepository.findAll()).thenReturn(List.of());

        List<AdminTourStatsDTO> result = service.getAllTourStats();

        assertTrue(result.isEmpty());
    }
}
