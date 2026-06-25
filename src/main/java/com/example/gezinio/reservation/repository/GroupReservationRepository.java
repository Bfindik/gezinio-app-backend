package com.example.gezinio.reservation.repository;

import com.example.gezinio.reservation.model.GroupReservation;
import com.example.gezinio.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupReservationRepository extends JpaRepository<GroupReservation, Long> {

    Optional<GroupReservation> findByGroupCode(String groupCode);

    boolean existsByGroupCode(String groupCode);

    List<GroupReservation> findByTourId(Long tourId);

    List<GroupReservation> findByStatus(ReservationStatus status);
}