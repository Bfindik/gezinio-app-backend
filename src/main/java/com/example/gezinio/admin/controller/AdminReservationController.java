package com.example.gezinio.admin.controller;

import com.example.gezinio.admin.dto.AdminReservationDTO;
import com.example.gezinio.admin.dto.ReservationFilterRequest;
import com.example.gezinio.admin.service.AdminReservationService;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.model.ReservationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Reservations", description = "Administrative reservation search and lifecycle management")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @Autowired
    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping
    @Operation(summary = "Search reservations using optional status, type, user, tour, and date filters")
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
    @Operation(summary = "Get a reservation by ID with admin-level detail")
    public ResponseEntity<AdminReservationDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.getReservationById(id));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending reservation")
    public ResponseEntity<AdminReservationDTO> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.confirmReservation(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<AdminReservationDTO> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(adminReservationService.cancelReservation(id));
    }
}
