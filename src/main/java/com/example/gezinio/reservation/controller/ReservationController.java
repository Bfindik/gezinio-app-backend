package com.example.gezinio.reservation.controller;

import com.example.gezinio.auth.security.UserPrincipal;
import com.example.gezinio.reservation.dto.ReservationCreateRequest;
import com.example.gezinio.reservation.dto.ReservationDTO;
import com.example.gezinio.reservation.dto.ReservationUpdateRequest;
import com.example.gezinio.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Individual reservation lifecycle: create, view, update, confirm, and cancel")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation for the current user")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request, currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation by its ID")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "List the current user's reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reservationService.getMyReservations(currentUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "List all reservations (ADMIN/AGENT only)")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing reservation owned by the current user")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request, currentUser.getId()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation owned by the current user")
    public ResponseEntity<ReservationDTO> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, currentUser.getId()));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Confirm a pending reservation (ADMIN/AGENT only)")
    public ResponseEntity<ReservationDTO> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }
}