package com.example.agentapp.excursion.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.excursion.dto.TourDTO;
import com.example.agentapp.excursion.dto.tour_requests.TourCreateRequest;
import com.example.agentapp.excursion.dto.tour_requests.TourSearchRequest;
import com.example.agentapp.excursion.dto.tour_requests.TourUpdateRequest;
import com.example.agentapp.excursion.service.TourService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    @Autowired
    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<TourDTO> createTour(
            @Valid @RequestBody TourCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TourDTO created = tourService.createTour(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping
    public ResponseEntity<List<TourDTO>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/active")
    public ResponseEntity<List<TourDTO>> getActiveTours() {
        return ResponseEntity.ok(tourService.getActiveTours());
    }

    @PostMapping("/search")
    public ResponseEntity<List<TourDTO>> searchTours(@RequestBody TourSearchRequest request) {
        return ResponseEntity.ok(tourService.searchTours(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<TourDTO> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourUpdateRequest request) {
        return ResponseEntity.ok(tourService.updateTour(id, request));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<TourDTO> publishTour(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.publishTour(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}