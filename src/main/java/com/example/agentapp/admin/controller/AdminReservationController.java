package com.example.agentapp.admin.controller;

import com.example.agentapp.admin.dto.AdminReservationDTO;
import com.example.agentapp.admin.dto.ReservationFilterRequest;
import com.example.agentapp.admin.service.AdminReservationService;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @Autowired
    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationDTO>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ReservationType type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ReservationFilterRequest filter = new ReservationFilterRequest();
        filter.setStatus(status);
        filter.setType(type);
        filter.setUserId(userId);
        filter.setTourId(tourId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);

        return ResponseEntity.ok(adminReservationService.getReservations(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminReservationDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.getReservationById(id));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AdminReservationDTO> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.confirmReservation(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AdminReservationDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.cancelReservation(id));
    }
}
