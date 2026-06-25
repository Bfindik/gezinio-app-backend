package com.example.gezinio.admin.service;

import com.example.gezinio.admin.dto.AdminReservationDTO;
import com.example.gezinio.admin.dto.ReservationFilterRequest;
import com.example.gezinio.admin.specification.ReservationSpecification;
import com.example.gezinio.auth.model.User;
import com.example.gezinio.payment.repository.PaymentRepository;
import com.example.gezinio.payment.repository.RefundRepository;
import com.example.gezinio.reservation.model.Reservation;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Autowired
    public AdminReservationService(ReservationRepository reservationRepository,
                                    PaymentRepository paymentRepository,
                                    RefundRepository refundRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    public List<AdminReservationDTO> getReservations(ReservationFilterRequest filter) {
        return reservationRepository.findAll(ReservationSpecification.fromFilter(filter))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AdminReservationDTO getReservationById(Long id) {
        return toDTO(findReservation(id));
    }

    @Transactional
    public AdminReservationDTO confirmReservation(Long id) {
        Reservation reservation = findReservation(id);
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot confirm a cancelled reservation");
        }
        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already completed");
        }
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        return toDTO(reservationRepository.save(reservation));
    }

    @Transactional
    public AdminReservationDTO cancelReservation(Long id) {
        Reservation reservation = findReservation(id);
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already cancelled");
        }
        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a completed reservation");
        }
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        return toDTO(reservationRepository.save(reservation));
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: " + id));
    }

    private AdminReservationDTO toDTO(Reservation r) {
        AdminReservationDTO dto = new AdminReservationDTO();
        dto.setId(r.getId());

        User user = r.getUser();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setUserEmail(user.getEmail());
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                " " + (user.getLastName() != null ? user.getLastName() : "");
        dto.setUserFullName(fullName.trim());

        dto.setReservationType(r.getReservationType());
        dto.setReservationStatus(r.getReservationStatus());
        dto.setPaymentStatus(r.getPaymentStatus());
        dto.setNumberOfParticipants(r.getNumberOfParticipants());
        dto.setTotalPrice(r.getTotalPrice());
        dto.setCurrency(r.getCurrency());
        dto.setSpecialRequests(r.getSpecialRequests());
        dto.setTravelStartDate(r.getTravelStartDate());
        dto.setTravelEndDate(r.getTravelEndDate());
        dto.setCreatedAt(r.getCreatedAt());

        if (r.getTour() != null) {
            dto.setTourId(r.getTour().getId());
            dto.setTourName(r.getTour().getName());
            dto.setDestination(r.getTour().getDestination());
        }
        if (r.getTransfer() != null) {
            dto.setTransferId(r.getTransfer().getId());
            dto.setTransferFromLocation(r.getTransfer().getFromLocation());
            dto.setTransferToLocation(r.getTransfer().getToLocation());
        }
        if (r.getHotel() != null) {
            dto.setHotelName(r.getHotel().getName());
        }
        if (r.getRoom() != null) {
            dto.setRoomName(r.getRoom().getName());
        }
        if (r.getGroupReservation() != null) {
            dto.setGroupCode(r.getGroupReservation().getGroupCode());
        }

        // Payment summary
        BigDecimal totalPaid = paymentRepository.sumCompletedPaymentsByReservationId(r.getId());
        BigDecimal totalRefunded = refundRepository.sumCompletedRefundsByReservationId(r.getId());
        BigDecimal remaining = r.getTotalPrice().subtract(totalPaid).add(totalRefunded);
        dto.setTotalPaid(totalPaid);
        dto.setTotalRefunded(totalRefunded);
        dto.setRemainingBalance(remaining.max(BigDecimal.ZERO));

        return dto;
    }
}
