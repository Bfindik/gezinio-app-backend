package com.example.gezinio.reservation.repository;

import com.example.gezinio.reservation.model.Reservation;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.model.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUserIdAndReservationStatus(Long userId, ReservationStatus status);

    List<Reservation> findByTourId(Long tourId);

    List<Reservation> findByTransferId(Long transferId);

    List<Reservation> findByGroupReservationId(Long groupReservationId);

    List<Reservation> findByReservationType(ReservationType type);

    List<Reservation> findByReservationStatus(ReservationStatus status);

    long countByReservationStatus(ReservationStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndReservationStatus(Long userId, ReservationStatus status);

    long countByTourId(Long tourId);

    long countByTourIdAndReservationStatus(Long tourId, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.createdAt >= :since")
    long countCreatedAfter(@Param("since") LocalDateTime since);
}