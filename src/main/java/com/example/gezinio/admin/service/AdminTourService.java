package com.example.gezinio.admin.service;

import com.example.gezinio.admin.dto.AdminTourStatsDTO;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTourService {

    private final TourRepository tourRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public AdminTourService(TourRepository tourRepository,
                             ReservationRepository reservationRepository,
                             PaymentRepository paymentRepository) {
        this.tourRepository = tourRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<AdminTourStatsDTO> getAllTourStats() {
        return tourRepository.findAll().stream()
                .map(this::toStatsDTO)
                .sorted(Comparator.comparingLong(AdminTourStatsDTO::getTotalBookings).reversed())
                .collect(Collectors.toList());
    }

    private AdminTourStatsDTO toStatsDTO(Tour tour) {
        AdminTourStatsDTO dto = new AdminTourStatsDTO();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDestination(tour.getDestination());
        dto.setTourType(tour.getTourType().name());
        dto.setTourStatus(tour.getTourStatus().name());
        dto.setPrice(tour.getPrice());
        dto.setCurrency(tour.getCurrency());
        dto.setCapacity(tour.getCapacity());
        dto.setDurationDays(tour.getDurationDays());
        dto.setCreatedAt(tour.getCreatedAt());

        Long tourId = tour.getId();
        dto.setTotalBookings(reservationRepository.countByTourId(tourId));
        dto.setPendingBookings(reservationRepository.countByTourIdAndReservationStatus(tourId, ReservationStatus.PENDING));
        dto.setConfirmedBookings(reservationRepository.countByTourIdAndReservationStatus(tourId, ReservationStatus.CONFIRMED));
        dto.setCancelledBookings(reservationRepository.countByTourIdAndReservationStatus(tourId, ReservationStatus.CANCELLED));
        dto.setTotalRevenue(paymentRepository.sumCompletedPaymentsByTourId(tourId));

        return dto;
    }
}
