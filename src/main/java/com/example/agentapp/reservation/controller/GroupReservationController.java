package com.example.agentapp.reservation.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.reservation.dto.GroupReservationCreateRequest;
import com.example.agentapp.reservation.dto.GroupReservationDTO;
import com.example.agentapp.reservation.service.ReservationService;
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
@RequestMapping("/api/group-reservations")
@Tag(name = "Group Reservations", description = "Group reservation management (ADMIN/AGENT only)")
public class GroupReservationController {

    private final ReservationService reservationService;

    @Autowired
    public GroupReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Create a new group reservation")
    public ResponseEntity<GroupReservationDTO> createGroupReservation(
            @Valid @RequestBody GroupReservationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createGroupReservation(request, currentUser.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Get a group reservation by its ID")
    public ResponseEntity<GroupReservationDTO> getGroupReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getGroupReservationById(id));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Get a group reservation by its booking code")
    public ResponseEntity<GroupReservationDTO> getGroupReservationByCode(@PathVariable String code) {
        return ResponseEntity.ok(reservationService.getGroupReservationByCode(code));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "List all group reservations")
    public ResponseEntity<List<GroupReservationDTO>> getAllGroupReservations() {
        return ResponseEntity.ok(reservationService.getAllGroupReservations());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Cancel a group reservation")
    public ResponseEntity<GroupReservationDTO> cancelGroupReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelGroupReservation(id));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Confirm a pending group reservation")
    public ResponseEntity<GroupReservationDTO> confirmGroupReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmGroupReservation(id));
    }
}