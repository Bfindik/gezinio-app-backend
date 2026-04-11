package com.example.agentapp.admin.service;

import com.example.agentapp.admin.dto.DashboardStatsDTO;
import com.example.agentapp.excursion.model.TourStatus;
import com.example.agentapp.excursion.repository.TourRepository;
import com.example.agentapp.notification.model.NotificationStatus;
import com.example.agentapp.notification.model.NotificationType;
import com.example.agentapp.notification.repository.NotificationLogRepository;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.payment.repository.RefundRepository;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.repository.ReservationRepository;
import com.example.agentapp.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TourRepository tourRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final NotificationLogRepository notificationLogRepository;

    @Autowired
    public AdminDashboardService(UserRepository userRepository,
                                  ReservationRepository reservationRepository,
                                  TourRepository tourRepository,
                                  PaymentRepository paymentRepository,
                                  RefundRepository refundRepository,
                                  NotificationLogRepository notificationLogRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.tourRepository = tourRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.notificationLogRepository = notificationLogRepository;
    }

    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // User metrics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByEnabled(true));
        stats.setLockedUsers(userRepository.findByAccountNonLocked(false).size());
        stats.setNewUsersLast30Days(userRepository.findByCreatedAtAfter(thirtyDaysAgo).size());

        // Reservation metrics
        stats.setTotalReservations(reservationRepository.count());
        stats.setPendingReservations(reservationRepository.countByReservationStatus(ReservationStatus.PENDING));
        stats.setConfirmedReservations(reservationRepository.countByReservationStatus(ReservationStatus.CONFIRMED));
        stats.setCancelledReservations(reservationRepository.countByReservationStatus(ReservationStatus.CANCELLED));
        stats.setCompletedReservations(reservationRepository.countByReservationStatus(ReservationStatus.COMPLETED));
        stats.setNewReservationsLast30Days(reservationRepository.countCreatedAfter(thirtyDaysAgo));

        // Revenue metrics
        BigDecimal totalRevenue = paymentRepository.sumAllCompletedPayments();
        BigDecimal revenueThisMonth = paymentRepository.sumCompletedPaymentsSince(startOfMonth);
        BigDecimal totalRefunded = refundRepository.sumAllCompletedRefunds();
        stats.setTotalRevenue(totalRevenue);
        stats.setRevenueThisMonth(revenueThisMonth);
        stats.setTotalRefunded(totalRefunded);
        stats.setNetRevenue(totalRevenue.subtract(totalRefunded));

        // Tour metrics
        List<com.example.agentapp.excursion.model.Tour> allTours = tourRepository.findAll();
        stats.setTotalTours(allTours.size());
        stats.setActiveTours(allTours.stream().filter(t -> t.getTourStatus() == TourStatus.ACTIVE).count());
        stats.setDraftTours(allTours.stream().filter(t -> t.getTourStatus() == TourStatus.DRAFT).count());

        allTours.stream()
                .max(Comparator.comparingLong(t -> reservationRepository.countByTourId(t.getId())))
                .ifPresent(tour -> {
                    stats.setMostBookedTourName(tour.getName());
                    stats.setMostBookedTourCount(reservationRepository.countByTourId(tour.getId()));
                });

        // Notification metrics
        stats.setTotalEmailsSent(notificationLogRepository.findByStatus(NotificationStatus.SENT).stream()
                .filter(n -> n.getType() == NotificationType.EMAIL).count());
        stats.setFailedEmails(notificationLogRepository.findByStatus(NotificationStatus.FAILED).stream()
                .filter(n -> n.getType() == NotificationType.EMAIL).count());

        return stats;
    }
}
