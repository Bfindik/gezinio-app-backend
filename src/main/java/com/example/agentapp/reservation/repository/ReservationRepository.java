package com.example.agentapp.reservation.repository;

import com.example.agentapp.reservation.model.Reservation;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUserIdAndReservationStatus(Long userId, ReservationStatus status);

    List<Reservation> findByTourId(Long tourId);

    List<Reservation> findByTransferId(Long transferId);

    List<Reservation> findByGroupReservationId(Long groupReservationId);

    List<Reservation> findByReservationType(ReservationType type);

    List<Reservation> findByReservationStatus(ReservationStatus status);
}