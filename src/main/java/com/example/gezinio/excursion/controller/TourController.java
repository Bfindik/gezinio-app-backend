package com.example.gezinio.excursion.controller;

import com.example.gezinio.auth.security.UserPrincipal;
import com.example.gezinio.excursion.dto.TourDTO;
import com.example.gezinio.excursion.dto.tour_requests.TourCreateRequest;
import com.example.gezinio.excursion.dto.tour_requests.TourSearchRequest;
import com.example.gezinio.excursion.dto.tour_requests.TourUpdateRequest;
import com.example.gezinio.excursion.service.TourService;
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
@RequestMapping("/api/tours")
@Tag(name = "Tours", description = "Tour catalog: create, search, publish, and manage tours")
public class TourController {

    private final TourService tourService;

    @Autowired
    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Create a new tour (initially in DRAFT status)")
    public ResponseEntity<TourDTO> createTour(
            @Valid @RequestBody TourCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TourDTO created = tourService.createTour(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tour by its ID")
    public ResponseEntity<TourDTO> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping
    @Operation(summary = "List all tours")
    public ResponseEntity<List<TourDTO>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/active")
    @Operation(summary = "List only active tours")
    public ResponseEntity<List<TourDTO>> getActiveTours() {
        return ResponseEntity.ok(tourService.getActiveTours());
    }

    @PostMapping("/search")
    @Operation(summary = "Search tours with filter criteria")
    public ResponseEntity<List<TourDTO>> searchTours(@RequestBody TourSearchRequest request) {
        return ResponseEntity.ok(tourService.searchTours(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Update an existing tour")
    public ResponseEntity<TourDTO> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourUpdateRequest request) {
        return ResponseEntity.ok(tourService.updateTour(id, request));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Publish a tour, transitioning it from DRAFT to ACTIVE")
    public ResponseEntity<TourDTO> publishTour(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.publishTour(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a tour by its ID (ADMIN only)")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}