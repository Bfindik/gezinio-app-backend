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

import java.util.List;

@RestController
@RequestMapping("/api/group-reservations")
public class GroupReservationController {

    private final ReservationService reservationService;

    @Autowired
    public GroupReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GroupReservationDTO> createGroupReservation(
            @Valid @RequestBody GroupReservationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createGroupReservation(request, currentUser.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GroupReservationDTO> getGroupReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getGroupReservationById(id));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GroupReservationDTO> getGroupReservationByCode(@PathVariable String code) {
        return ResponseEntity.ok(reservationService.getGroupReservationByCode(code));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<GroupReservationDTO>> getAllGroupReservations() {
        return ResponseEntity.ok(reservationService.getAllGroupReservations());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GroupReservationDTO> cancelGroupReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelGroupReservation(id));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GroupReservationDTO> confirmGroupReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmGroupReservation(id));
    }
}